# Deploy Paytrack to AWS with GitHub Actions

This guide gets your Paytrack microservices running on an **AWS EC2 server**
and **auto-deploys on every push to `main`** using GitHub Actions.

**How the whole thing works (read this once):**
Your project is a stack of 10 containers (2 Postgres DBs, Kafka + Zookeeper,
Kafka UI, and 5 Spring Boot apps). You already run it locally with
`docker compose`. Here you do the same thing, but on an AWS server.
GitHub Actions is just the robot that, on every push, connects to that server
and runs: *pull latest code → build JARs → docker compose up*.

---

## Step 1 — Create an AWS account

1. Go to <https://aws.amazon.com> and click **Create an AWS Account**.
2. Follow the signup (email, password, credit card for billing). The root user is fine for learning.
3. Log into the AWS console.

> Why: AWS is where your server lives. Everything you deploy will run on their machines, not your laptop.

---

## Step 2 — Create an EC2 server (your "cloud computer")

1. In the AWS console, search **EC2** and open it.
2. Click **Launch instance**.
3. Fill in the launch form:
   - **Name:** `paytrack-server`
   - **Application and OS Image:** **Ubuntu Server 24.04 LTS**
   - **Instance type:** `t3.large` (2 CPU, 8 GB RAM)
   - **Key pair:** click *Create new key pair* → name it `paytrack-key` → type **RSA** → download the `.pem` file.
   - **Storage:** change size to **30 GB**.
4. Click **Launch instance**.

> Why these choices:
> - **t3.large** — your stack runs 10 containers. The free tier `t2.micro` (1 GB RAM) will run out of memory. 8 GB is the safe minimum.
> - **RSA key pair** — this `.pem` file is the *password* to get into your server. AWS keeps the other half. Never share it, never lose it (you can't re-download it).
> - **30 GB disk** — the default 8 GB fills up fast with 10 Docker images + Maven's build output.

---

## Step 3 — Open the firewall (Security Groups)

1. In the EC2 console, go to **Network & Security → Security Groups**.
2. Click the security group attached to your instance.
3. Click **Edit inbound rules** and add:

| Type         | Port | Source    | Why                                          |
|--------------|-----:|-----------|----------------------------------------------|
| SSH          | 22   | My IP     | Lets you (and GitHub) connect to the server  |
| Custom TCP   | 8085 | Anywhere  | Your API Gateway — public entry point        |
| Custom TCP   | 8761 | Anywhere  | Eureka dashboard (development/debugging)     |
| Custom TCP   | 8888 | Anywhere  | Spring Cloud Config server (like 8761)       |
| Custom TCP   | 8090 | Anywhere  | Kafka UI (like 8761)                         |

4. Click **Save rules**.

> Why: AWS blocks all incoming traffic by default. Port 22 must be open so the
> GitHub Actions robot (and you) can SSH in. 8085 is the only port your app
> actually needs to be public; the others are for debugging. For production,
> you would only expose 8085.

---

## Step 4 — Connect to the server and install Docker

1. On the **Instances** page, copy your server's **Public IPv4 address**.
2. Open a terminal on your PC and connect:

   ```
   ssh -i C:\path\to\paytrack-key.pem ubuntu@<YOUR_PUBLIC_IP>
   ```

   > Windows may complain the key file permissions are too open. Fix with:
   > `icacls paytrack-key.pem /inheritance:r /grant:r "%USERNAME%:R"` then retry.

3. Now that you're *inside* the server, install Docker and Maven with one command:

   ```
   curl -fsSL https://get.docker.com | sh
   ```

4. Give your user permission to run Docker without `sudo`, and install the **JDK, then Maven**:

   ```
   sudo usermod -aG docker $USER
   sudo apt install -y openjdk-17-jdk
   sudo apt install -y maven
   exit
   ```

5. Reconnect with the same `ssh` command, then verify everything installed (you must see **17**):

   ```
   java -version
   docker --version
   docker compose version
   mvn -version
   ```

6. Clone your project onto the server and test the build:

   ```
   cd ~
   git clone https://github.com/MarwaneOukacha/paytrack.git
   cd paytrack
   mvn -q clean package -DskipTests
   ```

   > If your repo is **private**, Git will ask for credentials. Fix: create a
   > Personal Access Token at GitHub → *Settings → Developer settings → Personal
   > access tokens* with the `repo` scope, then clone with:
   > `git clone https://<your-username>:<TOKEN>@github.com/MarwaneOukacha/paytrack.git`

7. Run the stack once manually to prove it works on AWS:

   ```
   docker compose up -d --build
   ```

   Wait ~2 minutes, then open `http://<YOUR_PUBLIC_IP>:8085` and
   `http://<YOUR_PUBLIC_IP>:8761` in your browser. If they respond, your whole
   stack works on the server. Shut it down to keep costs tiny while we set up CI:

   ```
   docker compose down
   ```

> Why each step:
> - **get.docker.com** — installs Docker + Compose plugin automatically (instead of typing many apt commands by hand).
> - **usermod -aG docker** — lets the `ubuntu` user run docker without sudo (compose/GitHub Actions run as that user).
> - **openjdk-17-jdk** — every module in this project compiles for **Java 17** (`java.version=17` in all 6 POMs), so the build machine needs a JDK 17. A plain Maven install only brings a JRE, which cannot compile — that's why the JDK comes first.
> - **maven** — there is no Maven wrapper at the project root (only inside each module), so we install Maven globally to build all 6 modules.
> - **Test locally on the server first** — if a step fails here, it will also fail in GitHub Actions. Better to discover it now while you have a terminal.

---

## Step 5 — Store your credentials in GitHub Secrets

1. On GitHub, open your repo → **Settings → Secrets and variables → Actions**.
2. Click **New repository secret** and add these three (one at a time):

| Secret name  | Value                                                        |
|--------------|--------------------------------------------------------------|
| `EC2_HOST`   | Your server's **Public IPv4** address                        |
| `EC2_USERNAME` | `ubuntu`                                                   |
| `EC2_SSH_KEY` | The **entire** content of `paytrack-key.pem` (from `-----BEGIN RSA PRIVATE KEY-----` to the END line) |

> Why: your workflow file needs to connect to the server, but you must NEVER
> put passwords/keys directly in code. GitHub Secrets keeps them encrypted and
> injects them only at run time. Anyone who reads your repo can't see them.

---

## Step 6 — Add the GitHub Actions workflow

The file `.github/workflows/deploy.yml` (already created for you in this repo)
is your automation robot. It says:

> "On every push to `main`: SSH into the server, clone/pull the code, build the
> JARs, run `docker compose up -d --build`, and print the container status."

Each section of that file is explained with comments, but the key parts are:

- **`on: push: branches: [main]`** — what triggers the deploy (every push to main).
- **`workflow_dispatch`** — lets you also press a **"Run workflow"** button manually to test.
- **`appleboy/ssh-action`** — the third-party action that opens an SSH connection using `EC2_HOST`, `EC2_USERNAME`, `EC2_SSH_KEY`.
- **`script:`** — the exact command sequence that runs *on the server*:
  1. Clone the repo if the folder doesn't exist (first deploy).
  2. `git fetch` + `git reset --hard origin/main` to sync to the latest code.
  3. `mvn -q clean package -DskipTests` to build all 6 modules.
  4. `docker compose up -d --build` to rebuild and start the 10 containers.
  5. `docker compose ps` so the deploy log shows the final state.

---

## Step 7 — Push and watch it deploy

1. Commit the workflow file:

   ```
   git add .github/workflows/deploy.yml
   git commit -m "add deploy workflow"
   git push
   ```

2. Open the **Actions** tab on GitHub. You'll see the workflow running.
3. Click it → click the **deploy** job → follow the live logs.
   - First run takes ~10 minutes (the server downloads Maven dependencies and Docker images it has never seen before).
   - Later runs take ~2–3 minutes.
4. When the run is green, open `http://<YOUR_PUBLIC_IP>:8085` — you're live! 🎉

From now on, **every push to `main` deploys automatically.**

---

## Useful commands when something goes wrong

Run these over SSH (same `ssh` command as in Step 4):

| Problem                                | Command                                          |
|----------------------------------------|--------------------------------------------------|
| See what is running                    | `docker compose ps`                              |
| See crash details of one service       | `docker compose logs -f payment-service`         |
| Server is low on RAM/disk              | `free -h` · `df -h`                              |
| Restart the whole stack                | `docker compose up -d`                           |
| Stop everything (save money)           | `docker compose down`                            |
| Redeploy manually from the server      | `cd ~/paytrack && git pull && mvn -q clean package -DskipTests && docker compose up -d --build` |

---

## Known things to fix before going "real" production

1. **Hardcoded config hostname** — `payment-service` points at
   `configserver:http://config-server:8888`. Fine here (same Docker network as
   locally), but breaks on ECS/EKS.
2. **Hardcoded credentials** — DB passwords are `postgres/postgres`. Move them to
   AWS Secrets Manager before exposing anything sensitive.
3. **Committed `target/*.jar`** — built JAR files are tracked in git. Not harmful,
   but a real project would gitignore them and build in CI.
4. **Everything on one machine** — fine for learning. For high availability you'd
   move to **ECS Fargate** (managed containers) + **RDS** (managed Postgres) +
   **MSK** (managed Kafka) and deploy with ECR instead of SSH+compose.