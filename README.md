### News Contextual System

This repository contains two main components:

- LLM Service (Python FastAPI) – Handles NLP tasks like entity extraction, summarization, and text analysis.

- News Backend (Java Spring Boot) – Provides REST APIs to serve news articles, user interactions, trending news, and integrates with the LLM service.

### Prerequisites
-Python 3.9+

-Java 17+ (or compatible JDK)

-Maven

-Git

-Optional: Postgres or any other database (update application.properties accordingly)

### Setup LLM Service (Python)

Navigate to the Python service:

- cd llm_service


Create a virtual environment:

- python3 -m venv venv
- source venv/bin/activate   # Linux/macOS
- venv\Scripts\activate      # Windows


### Install dependencies:

- pip install -r requirements.txt


### Create a .env file in llm_service/app with your keys or configurations:

- OPENAI_API_KEY=<your_api_key>

### Run the service:

- uvicorn app.main:app --host 0.0.0.0 --port 8001


The LLM service will be accessible at http://localhost:8001.

### Setup News Backend (Java Spring Boot)

Navigate to the backend:

- cd news_backend


Build and run the backend:

- mvn spring-boot:run


The backend will start on http://localhost:8080 and connect to the LLM service at port 8001.

Project docs
- https://www.notion.so/Context-based-news-retrieval-system-LLM-Layer-259b85e4b7968091aeb0c948e82de0fd?source=copy_link
- https://www.notion.so/Context-based-news-retrieval-system-Model-layer-259b85e4b796802fb5a1ec064407c789?source=copy_link
- https://www.notion.so/Context-based-news-retrieval-system-Application-Layer-259b85e4b79680d99025dc400b5e5b86?source=copy_link

<img width="890" height="903" alt="Screenshot 2025-08-24 at 7 24 39 PM" src="https://github.com/user-attachments/assets/cfc88e92-a040-4b56-a06c-f5dff8a93c35" />

<img width="891" height="911" alt="Screenshot 2025-08-24 at 7 24 24 PM" src="https://github.com/user-attachments/assets/a93dc32b-4fba-4d42-8988-21e592d03d2c" />

<img width="890" height="903" alt="Screenshot 2025-08-24 at 7 24 39 PM" src="https://github.com/user-attachments/assets/b9757716-bfb2-4c51-91f5-d59e72c8b146" />
