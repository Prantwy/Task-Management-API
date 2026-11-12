# TaskFlow API

A Java 21 / Spring Boot REST API for managing tasks. It includes an optional AI-powered task summary endpoint and is packaged as a Docker image using a multi-stage Maven build.

## Live application

The Render endpoint provided for this assignment is [`https://task-management-api-0kea.onrender.com/api/tasks`](https://task-management-api-0kea.onrender.com/api/tasks).

**Deployment verification:** I could not verify the live service from this environment. A request to the URL failed with a DNS resolution error (`Could not resolve host`), so the endpoint's current availability is unknown.

## API

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/tasks` | List tasks (an empty list if none have been created) |
| `GET` | `/api/tasks/{id}` | Get a task by ID |
| `POST` | `/api/tasks` | Create a task |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `PUT` | `/api/tasks/{id}/complete` | Mark a task complete |
| `GET` | `/api/tasks/summary` | Generate a plain-text AI summary of the task list |

Example request to create a task:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Review project","completed":false}'
```

The current repository implementation stores tasks in memory; they are not persisted across application restarts. The summary endpoint uses the configured OpenRouter-compatible AI service and requires an `OPENAI_API_KEY` environment variable.

## Run locally

Requires Java 21 and Docker (or Maven to run without Docker). From the repository root:

```bash
docker build -t taskflow:latest .
docker run --rm -p 8080:8080 -e OPENAI_API_KEY="$OPENAI_API_KEY" taskflow:latest
```

Then test the API:

```bash
curl http://localhost:8080/api/tasks
```

To run with Maven instead, set `OPENAI_API_KEY` in your environment and run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The Dockerfile builds the JAR in a Maven stage and runs it on the Java 21 JRE image, exposing port `8080`.

## Deploy to Render

1. Create a new **Web Service** in Render and connect this GitHub repository.
2. Select Docker as the runtime/build method so Render uses the repository's `Dockerfile`.
3. Set the `OPENAI_API_KEY` environment variable in the Render service settings.
4. Deploy and use the service URL provided by Render. The application listens on port `8080`.
5. Verify the deployment with `curl https://<your-render-service>.onrender.com/api/tasks` and replace the live URL above if Render provides a different URL.

## Screenshots

The assignment requests screenshots of the active Render deployment and a successful request to the live API. Those screenshots are not present in the repository, and no deployment dashboard was available in this session. Add the screenshots at the paths below and commit them to complete this evidence section:

![Render dashboard showing an active deployment](screenshots/render-dashboard.png)

![Live API response](screenshots/render-api-response.png)

## Errors and incomplete items

- The Render endpoint supplied by the assignment could not be reached from this environment: DNS lookup failed (`Could not resolve host`). This does not establish whether the deployment itself is down; its live status remains unverified.
- The Render dashboard and live API screenshots have not been added because no dashboard or successful live response was available here.
- No stretch goal is documented in the repository.
