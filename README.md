🎬 Movie Recommender
Description
The Movie Recommender is a web-based application designed to help users find movies tailored to their preferences. By selecting their current mood, desired pace, and whether the movie should be family-friendly, users receive personalized recommendations. The app uses a backend server to fetch movie data and provides detailed information for each movie, including where to watch and a trailer link.
## Table of Contents


## Features

Mood-Based Recommendations: Choose a mood, such as Happy, Sad, or Romantic, to tailor suggestions.
Pace Preference: Select the desired pace of the movie (Fast, Medium, or Slow).
Family-Friendly Filter: Decide if the movie should be suitable for all ages.
Detailed Information: View cast, genres, description, streaming platforms, and trailers for each movie.
Dynamic UI: Smooth transitions and responsive design for various screen sizes.



## Installation

To get started with the Literary Insight Engine, follow these steps:

Clone the repository:
git clone [git@github.com:RahulRR-10/LiteraryInsightEngine.git](https://github.com/RahulRR-10/LiteraryInsightEngine.git)
cd LiteraryInsightEngine

Install the required dependencies:
pip install -r requirements.txt

Download necessary NLTK data:
import nltk
nltk.download('stopwords')

Set up environment variables by creating a .env file for Azure and OpenAI API keys:
OPENAI_API_KEY=your_openai_api_key
AZURE_KEY_VAULT_NAME=your_azure_key_vault_name
AZURE_CLIENT_ID=your_azure_client_id
AZURE_TENANT_ID=your_azure_tenant_id
AZURE_CLIENT_SECRET=your_azure_client_secret

Run the application:
flask run


## Folder Structure

The project follows a structured folder organization:

```
your_project/
├── backend/
│   ├── app.py                 // Main Flask app file
│   ├── static/
│   │   └── wordclouds/         // Generated word clouds
│   ├── templates/
│   │   └── all_results.html    // HTML template for displaying results
│   ├── uploads/                // Directory for uploaded text files
│   └── requirements.txt        // Python dependencies
├── frontend/
│   ├── public/                 // Public folder for frontend assets
│   ├── src/
│   │   ├── components/         // React components
│   │   │   ├── FileUpload.js   // Component for file upload
│   │   │   ├── UtilitySelector.js // Component for selecting text utilities
│   │   │   ├── WordCloud.js    // Component for displaying word clouds
│   │   │   └── ResultsViewer.js // Component for displaying results
│   │   ├── App.js              // Main App component
│   │   └── index.js            // Entry point for React app
│   ├── package.json            // Node.js dependencies
│   └── .env                    // Environment variables for React
├── .gitignore                  // Files to ignore in git
└── README.md                   // Project documentation

```



## Usage

    Upload a text file on the Dashboard.
    Select a utility (e.g., Word Cloud, Sentiment Analysis, Geospatial Mapping) from the available options.
    View the results on the respective results page with interactive visuals and detailed analysis.
    Navigate between utilities without re-uploading the file.

Word Cloud

    After uploading your text, click on the Word Cloud tile to visualize the most frequent words in a word cloud format.

Sentiment Analysis

    Explore the emotional tone of your text. The results page will display sentiment polarity and subjectivity with a corresponding graph.

Geospatial Mapping

    Visualize the locations mentioned in your text on an interactive map, allowing you to explore the geographical aspect of literature.

# UI Upgrade: React Integration

The Literary Insight Engine is getting an improved, modern, and responsive UI by integrating React. This update aims to enhance user interactivity and provide a more streamlined experience.

With the React upgrade, users can expect:

    Faster Interactions: With real-time UI updates and no full-page reloads.
    Cleaner Design: More modular and maintainable interface.
    Improved Responsiveness: Optimized for use across various devices, including tablets and mobile phones.
    Engaging User Experience: Enhanced interactivity for utilities like word clouds, maps, and sentiment analysis.

We are continuously working to refine the user experience, and this React integration will make the Literary Insight Engine more intuitive and powerful.


## Future Enhancements

    Enhanced Topic Modeling: Deeper insights using more advanced algorithms.
    Stylometry: Authorial style comparison and visualization.
    Radar Charts for Emotions: Display emotional tones in a radar chart for better understanding of the emotional structure of the text.
    More Interactive Visualizations: Continued improvements to charts and visualizations with Plotly and D3.js.
    Chatbot Feature: Integrating Azure OpenAI for a literary-focused chatbot to provide deeper insights and answers based on the uploaded text.

## Contributing

We welcome contributions to the Literary Insight Engine! Please follow these steps:

    Fork the repository.
    Create a new branch (git checkout -b feature/new-feature).
    Commit your changes (git commit -m 'Add new feature').
    Push to the branch (git push origin feature/new-feature).
    Open a pull request and describe your changes.

