# Disaster Preparedness Application

## Description

This application provides a disaster preparedness plan based on a user-provided address. It fetches real-time data from various sources, including weather conditions, geographical information, and natural event data from NASA. The application is built as a Spring Boot backend with a simple HTML frontend. It also includes a chatbot feature that allows users to ask questions about the generated disaster plan.

## Tech Stack

*   **Backend:** Java, Spring Boot
*   **Frontend:** HTML, CSS, JavaScript
*   **Build Tool:** Maven
*   **APIs:**
    *   Geoapify API (for geocoding)
    *   OpenWeatherMap API (for weather data)
    *   Open-Meteo Geocoding API (for geographical data)
    *   NASA EONET (for natural event data)
    *   OpenRouter AI (for generating the disaster plan and chatbot responses)

## Getting Started

### Prerequisites

*   Java 8 or higher
*   Maven
*   An internet connection

### Installation

1.  Clone the repository.
2.  Create a `.env` file in the root of the project and add the following API keys:

    ```
    OPENROUTER_API_KEY="your_openrouter_api_key"
    WEATHER_API_KEY="your_openweathermap_api_key"
    NASA_API_KEY="your_nasa_api_key"
    GEOAPIFY_API_KEY="your_geoapify_api_key"
    ```

### Running the Application

1.  **Build the application:**
    ```bash
    mvn clean install
    ```

2.  **Run the backend server:**
    ```bash
    java -jar target/disasterprep-1.0-SNAPSHOT.jar
    ```

3.  **Open the frontend:** Open the `index.html` file in your web browser.

## API Endpoints

The application exposes the following REST endpoints:

### `/evaluate`

*   **Method:** `POST`
*   **Content-Type:** `text/plain`
*   **Request Body:** A plain text string containing the address to be evaluated.
*   **Response:** A JSON object containing the raw data and the disaster plan.

    **Example Request:**
    ```bash
    curl -X POST -H "Content-Type: text/plain" -d "Chennai, Tamil Nadu" http://localhost:8080/evaluate
    ```

### `/chat`

*   **Method:** `POST`
*   **Content-Type:** `text/plain`
*   **Request Body:** A plain text string containing the user's message for the chatbot.
*   **Response:** A plain text string with the chatbot's response.

    **Example Request:**
    ```bash
    curl -X POST -H "Content-Type: text/plain" -d "What is the current threat level?" http://localhost:8080/chat
    ```

## Deployment

This application can be deployed to any platform that supports Java applications, such as Render, Heroku, or AWS Elastic Beanstalk.

### Build Command

```bash
mvn clean install
```

### Start Command

```bash
java -jar target/disasterprep-1.0-SNAPSHOT.jar
```

Remember to set the environment variables for the API keys in your deployment environment.