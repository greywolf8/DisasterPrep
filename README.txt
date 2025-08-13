# Disaster Preparedness Application

A comprehensive disaster preparedness and management application that provides real-time disaster information, risk assessment, and personalized preparedness plans based on user location.

## 🌟 Features

- **Location-based Risk Assessment**: Analyzes disaster risks based on the user's location
- **Real-time Data Integration**: Fetches and processes data from multiple sources including:
  - NASA EONET for natural events
  - Weather APIs for real-time weather conditions
  - Soil moisture and precipitation data
- **Interactive Map Interface**: Visualizes disaster data on an interactive map
- **AI-Powered Chatbot**: Answers questions about disaster preparedness and provides guidance
- **Personalized Preparedness Plans**: Generates customized disaster preparedness plans

## 🛠️ Tech Stack

- **Backend**: Java 8+, Spring Boot
- **Frontend**: HTML5, CSS3, JavaScript
- **Build Tool**: Maven
- **APIs**:
  - NASA EONET API
  - OpenWeatherMap API
  - Geoapify API
  - OpenRouter AI
  - NASA CMR and SMAP

## 🚀 Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.0 or higher
- API keys for the following services:
  - OpenRouter AI
  - OpenWeatherMap
  - NASA Earthdata
  - Geoapify

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd disaster_APP1
   ```

2. **Set up environment variables**
   Create a `.env` file in the project root with the following variables:
   ```env
   OPENROUTER_API_KEY=your_openrouter_api_key
   WEATHER_API_KEY=your_openweathermap_api_key
   NASA_API_KEY=your_nasa_api_key
   GEOAPIFY_API_KEY=your_geoapify_api_key
   EARTHDATA_USERNAME=your_earthdata_username
   EARTHDATA_PASSWORD=your_earthdata_password
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   Open your browser and navigate to `http://localhost:8080`

## 📂 Project Structure

```
disaster_APP1/
├── src/
│   └── main/
│       ├── java/com/example/
│       │   ├── AddressToCoordsClient.java  # Geocoding service
│       │   ├── CmrClient.java              # NASA CMR client
│       │   ├── CombinedGeoClient.java      # Combined geolocation services
│       │   ├── DisasterChatbot.java        # AI chatbot implementation
│       │   ├── IMERGApiClient.java         # IMERG precipitation data
│       │   ├── Main.java                   # Main application class
│       │   ├── NasaEonetClient.java        # NASA EONET client
│       │   ├── OpenRouterClient.java       # OpenRouter AI integration
│       │   ├── PrecipitationReader.java    # Precipitation data processor
│       │   └── WeatherApiClient.java       # Weather data service
│       └── resources/
│           └── application.properties      # Application configuration
├── target/                                 # Compiled classes and build output
├── index.html                              # Main web interface
├── maps.html                               # Interactive map interface
└── pom.xml                                 # Maven configuration
```

## 🤖 Using the Chatbot

The application includes an AI-powered chatbot that can answer questions about disaster preparedness. Simply type your question in the chat interface, and the bot will provide relevant information and guidance.

## 📊 Data Sources

- **NASA EONET**: Natural event tracking (wildfires, storms, etc.)
- **OpenWeatherMap**: Current and forecasted weather data
- **NASA SMAP**: Soil moisture data for flood risk assessment
- **IMERG**: Integrated Multi-satellitE Retrievals for GPM (Global Precipitation Measurement)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- NASA for providing open data APIs
- OpenWeatherMap for weather data
- The Spring Boot and Maven communities
- All open-source libraries used in this project
