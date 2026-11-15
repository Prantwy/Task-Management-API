# TaskFlow API

Task Management API aka TaskFlow API was originally created for Module 3 assignment. As the name suggests, it is a Spring Boot task-management API with:

1. A layered architecture consisting of controller, model, repository and service layers to manage the flow of tasks in the backend
2. REST API endpoints  to create, read, update, delete, and summarise tasks in the backend.


 As there is no database, tasks are stored in memory, so they will not persist after the application stops or restarts.

For Module 4 assignment, TaskFlow API is containerised as a Docker image using a multi-stage Maven build and deployed on Render.



## Live Application URL

Render live application URL: https://task-management-api-0kea.onrender.com

## Screenshots
### Render dashboard with active deployment
![Render dashboard with active deployment](screenshots/renderdashboard.png)
![alt text](screenshots/activedeployment.png)

### Deployment to Render succeeded
![alt text](screenshots/deploysucceeded.png)

### Live application URL is working
![Live API response](screenshots/liveapplicationworking.png)


## REST API Endpoints

| Method     | Path                          | Description                                          |
| ---------- | ----------------------------- | ---------------------------------------------------- |
| `GET`    | `/api/tasks`                | List tasks (an empty list if none have been created) |
| `GET`    | `/api/tasks/{id}`           | Get a task by ID                                     |
| `POST`   | `/api/tasks`                | Create a task                                        |
| `PUT`    | `/api/tasks/{id}`           | Update a task                                        |
| `DELETE` | `/api/tasks/{id}`           | Delete a task                                        |
| `PUT`    |  `/api/tasks/{id}/complete` | Mark a task complete                                 |
| `GET`    | `/api/tasks/summary`        | Generate a plain-text AI summary of the task list    |


Example request to create a task:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Test deployment","completed":false}'
```
The summary endpoint uses the configured OpenRouter OpenAI key and requires an `OPENAI_API_KEY` environment variable.

Keep the key in an environment variable in ~/.bashrc:
```bash
export OPENAI_API_KEY=(Copy_and_paste_your_OpenAI_API_key_here)
```
In your 'application.properties' file, refer to it like this: spring.ai.openai.api-key=${OPENAI_API_KEY} 

```bash
# Spring AI - OpenRouter Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.base-url=https://openrouter.ai/api/v1
spring.ai.openai.chat.model=openrouter/free
spring.ai.openai.embedding.model=nvidia/llama-nemotron-embed-vl-1b-v2:free
spring.ai.openai.embedding.encoding-format=float
```

## Run locally

Requires Java 21 and Docker (or Maven to run without Docker). From the repository root:

Build Docker image:
```bash
docker build -t taskflow:latest .
```
![alt text](screenshots/image.png)

Run the container:
```bash
docker run -p 8080:8080 taskflow:latest
```
![alt text](screenshots/container.png)

Then test the API:

```bash
curl http://localhost:8080/api/tasks
```
![alt text](screenshots/curllocalhosttest.png)

![alt text](screenshots/httplocalhosttest.png)



To run with Maven instead, set `OPENAI_API_KEY` in your environment and run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The Dockerfile builds the JAR in a Maven stage and runs it on the Java 21 JRE image, exposing port `8080`.


## Deploy to Render

1. Create a new **Web Service** in Render and connect this GitHub repository.
2. Select Docker as the runtime/build method so Render uses the repository's `Dockerfile`.
3. Set the `OPENAI_API_KEY` environment variable in the Render service settings.
4. The application listens on port `8080`. Set PORT as an environment variable to `8080`.
5. Test the live deployment with a POST request to create a task.
```bash
curl -X POST https://task-management-api-0kea.onrender.com/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test deployment","completed":true}'
```

6. Verify the deployment with `curl https://task-management-api-0kea.onrender.com/api/tasks` 

![alt text](screenshots/httpapitest.png)


  