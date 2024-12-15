

## main MovieRecommender.java file

```


import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

class Movie {
    private String title;
    private List<String> genres;
    private int year;
    private double rating;
    private String mood;
    private String pace;
    private boolean familyFriendly;
    private int length;

    public Movie(String title, List<String> genres, int year, double rating, 
                 String mood, String pace, boolean familyFriendly, int length) {
        this.title = title;
        this.genres = genres;
        this.year = year;
        this.rating = rating;
        this.mood = mood;
        this.pace = pace;
        this.familyFriendly = familyFriendly;
        this.length = length;
    }

    public String getTitle() { return title; }
    public List<String> getGenres() { return genres; }
    public int getYear() { return year; }
    public double getRating() { return rating; }
    public String getMood() { return mood; }
    public String getPace() { return pace; }
    public boolean isFamilyFriendly() { return familyFriendly; }
    public int getLength() { return length; }
}

class MovieDetails {
    private String title;
    private List<String> genres;
    private int year;
    private double rating;
    private String director;
    private List<String> cast;
    private String description;
    private List<String> streamingPlatforms;
    private String trailerUrl;

    public MovieDetails(Movie movie, String director, List<String> cast, 
                        String description, List<String> streamingPlatforms, 
                        String trailerUrl) {
        this.title = movie.getTitle();
        this.genres = movie.getGenres();
        this.year = movie.getYear();
        this.rating = movie.getRating();
        this.director = director;
        this.cast = cast;
        this.description = description;
        this.streamingPlatforms = streamingPlatforms;
        this.trailerUrl = trailerUrl;
    }

    public String getTitle() { return title; }
    public List<String> getGenres() { return genres; }
    public int getYear() { return year; }
    public double getRating() { return rating; }
    public String getDirector() { return director; }
    public List<String> getCast() { return cast; }
    public String getDescription() { return description; }
    public List<String> getStreamingPlatforms() { return streamingPlatforms; }
    public String getTrailerUrl() { return trailerUrl; }
}

class MovieDetailsProvider {
    private Map<String, MovieDetails> movieDetailsMap = new HashMap<>();

    public MovieDetailsProvider(String csvPath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(csvPath));
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 10) {
                    String title = parts[0];
                    int year = Integer.parseInt(parts[2]);
                    double rating = Double.parseDouble(parts[3]);
                    String director = parts[8];
                    List<String> cast = Arrays.asList(parts[9].split("\\|"));
                    String description = parts[10];
                    List<String> streamingPlatforms = Arrays.asList(parts[11].split("\\|"));
                    String trailerUrl = parts[12];

                    List<String> genres = Arrays.asList(parts[1].split("\\|"));
                    Movie movie = new Movie(title, genres, year, rating, "", "", true, 0);
                    
                    movieDetailsMap.put(title, new MovieDetails(
                        movie, director, cast, description, 
                        streamingPlatforms, trailerUrl
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading movie details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MovieDetails getMovieDetails(String title) {
        return movieDetailsMap.get(title);
    }
}

interface RecommendationStrategy {
    List<Movie> recommend(List<Movie> movies, Map<String, String> preferences);
}

class AdvancedRecommender implements RecommendationStrategy {
    @Override
    public List<Movie> recommend(List<Movie> movies, Map<String, String> preferences) {
        return movies.stream()
            .filter(movie -> matchesPreferences(movie, preferences))
            .sorted((m1, m2) -> Double.compare(
                calculateMatchScore(m2, preferences),
                calculateMatchScore(m1, preferences)))
            .collect(Collectors.toList());
    }

    private boolean matchesPreferences(Movie movie, Map<String, String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return true;
        }

        // Check for mood preference
        if (preferences.containsKey("mood") && !movie.getMood().equalsIgnoreCase(preferences.get("mood"))) {
            return false;
        }

        if (preferences.containsKey("pace") && !movie.getPace().equalsIgnoreCase(preferences.get("pace"))) {
            return false;
        }

        if (preferences.containsKey("familyFriendly")) {
            boolean familyFriendly = Boolean.parseBoolean(preferences.get("familyFriendly"));
            if (movie.isFamilyFriendly() != familyFriendly) {
                return false;
            }
        }

        return true;
    }

    private double calculateMatchScore(Movie movie, Map<String, String> preferences) {
        double score = movie.getRating();
        if (preferences.containsKey("mood") && movie.getMood().equalsIgnoreCase(preferences.get("mood"))) {
            score += 1.0;
        }
        return score;
    }
}

class MovieDatabase {
    private List<Movie> movies;
    private RecommendationStrategy recommendationStrategy;

    public MovieDatabase(RecommendationStrategy strategy) {
        this.movies = new ArrayList<>();
        this.recommendationStrategy = strategy;
    }

    public void loadMovies(String csvPath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(csvPath));
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1); // Ensure all fields are accounted for
                if (parts.length >= 8) {
                    String title = parts[0];
                    List<String> genres = Arrays.asList(parts[1].split("\\|"));
                    int year = Integer.parseInt(parts[2]);
                    double rating = Double.parseDouble(parts[3]);
                    String mood = parts[4];
                    String pace = parts[5];
                    boolean familyFriendly = Boolean.parseBoolean(parts[6]);
                    int length = Integer.parseInt(parts[7]);

                    movies.add(new Movie(title, genres, year, rating, mood, pace, familyFriendly, length));
                } else {
                    System.err.println("Skipping malformed line: " + lines.get(i));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading movies.csv: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numerical values: " + e.getMessage());
        }
    }

    public List<Movie> getRecommendations(Map<String, String> preferences) {
        return recommendationStrategy.recommend(movies, preferences);
    }
}

//edited till here

public class MovieRecommender {
    private static MovieDatabase movieDb;
    private static MovieDetailsProvider movieDetailsProvider;
    private static HttpServer server;
    private static final String JSON_RESPONSE_TEMPLATE =
        "{\"title\":\"%s\",\"year\":%d,\"rating\":%.1f,\"genres\":\"%s\"}";
    private static final int[] PORTS = {8080, 8081, 8082, 8083, 8084, 8085};

    public static void main(String[] args) {
        movieDb = new MovieDatabase(new AdvancedRecommender());
        movieDb.loadMovies("./movies.csv");

        // Initialize movie details provider
        movieDetailsProvider = new MovieDetailsProvider("./movies.csv");

        int selectedPort = -1;

        for (int port : PORTS) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                selectedPort = port;
                break;
            } catch (IOException e) {
                System.out.println("Port " + port + " is in use, trying next port...");
            }
        }

        if (server == null) {
            System.err.println("Could not find an available port. Please free up one of these ports: " + 
                             Arrays.toString(PORTS));
            System.exit(1);
        }

        try {
            server.createContext("/", new StaticFileHandler());
            server.createContext("/recommend", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                        BufferedReader br = new BufferedReader(isr);
                        String query = br.readLine();
                        Map<String, String> preferences = parseQueryString(query);

                        List<Movie> recommendations = movieDb.getRecommendations(preferences);
                        String jsonResponse = recommendations.stream()
                            .map(movie -> String.format(JSON_RESPONSE_TEMPLATE,
                                movie.getTitle(),
                                movie.getYear(),
                                movie.getRating(),
                                String.join(", ", movie.getGenres())))
                            .collect(Collectors.joining(",", "[", "]"));

                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        byte[] responseBytes = jsonResponse.getBytes();
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(responseBytes);
                        }
                    } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                        exchange.sendResponseHeaders(204, -1);
                    }
                }
            });

            server.createContext("/movie-details", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                        // Read the movie title from the request body
                        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                        BufferedReader br = new BufferedReader(isr);
                        String title = br.readLine();

                        // Retrieve movie details
                        MovieDetails details = movieDetailsProvider.getMovieDetails(title);

                        if (details != null) {
                            // Construct HTML response directly
                            String htmlResponse = constructMovieDetailsHtml(details);
                            exchange.getResponseHeaders().set("Content-Type", "text/html");
                            byte[] responseBytes = htmlResponse.getBytes();
                            exchange.sendResponseHeaders(200, responseBytes.length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(responseBytes);
                            }
                        } else {
                            String errorResponse = "Movie not found";
                            exchange.sendResponseHeaders(404, errorResponse.length());
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(errorResponse.getBytes());
                            }
                        }
                    } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                        exchange.sendResponseHeaders(204, -1);
                    }
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("Server successfully started on port " + selectedPort);
            System.out.println("Access the application at http://localhost:" + selectedPort);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop(0);
            }));
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String constructMovieDetailsHtml(MovieDetails details) {
        // Create a poster filename by replacing spaces with hyphens, removing special characters, and converting to lowercase
        String posterFilename = details.getTitle()
            .replaceAll("[^a-zA-Z0-9\\s]", "")  // Remove special characters
            .replace(" ", "-")
            .toLowerCase() + ".jpg";
        
        // Use a default poster if the specific movie poster doesn't exist
        String posterPath = String.format("posters/%s", posterFilename);
        String defaultPosterPath = "posters/default-poster.jpg";
        
        return String.format(
            "<div class='movie-details'>" +
            "<h2>%s (%d)</h2>" +
            "<div class='movie-details-content'>" +
            "<div class='movie-poster'>" +
            "<img src='%s' alt='%s Poster' onerror=\"this.onerror=null; this.src='%s';\">" +
            "</div>" +
            "<div class='movie-info'>" +
            "<p><strong>Genres:</strong> %s</p>" +
            "<p><strong>Director:</strong> %s</p>" +
            "<p><strong>Cast:</strong> %s</p>" +
            "<p><strong>Rating:</strong> ⭐ %.1f</p>" +
            "<p><strong>Description:</strong> %s</p>" +
            "<p><strong>Where to Watch:</strong> %s</p>" +
            "<a href='%s' target='_blank' class='trailer-btn'>Watch Trailer</a>" +
            "</div>" +
            "</div>" +
            "</div>",
            details.getTitle(), details.getYear(),
            posterPath, details.getTitle(), defaultPosterPath,
            String.join(", ", details.getGenres()),
            details.getDirector(),
            String.join(", ", details.getCast()),
            details.getRating(),
            details.getDescription(),
            String.join(", ", details.getStreamingPlatforms()),
            details.getTrailerUrl()
        );
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/favicon.ico")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            path = path.equals("/") ? "/index.html" : path;
            String filePath = "." + path;
            System.out.println("Serving file: " + filePath);
            try {
                byte[] response = Files.readAllBytes(Paths.get(filePath));
                String contentType = 
                    path.endsWith(".html") ? "text/html" :
                    path.endsWith(".css") ? "text/css" :
                    path.endsWith(".js") ? "application/javascript" :
                    path.endsWith(".jpg") ? "image/jpeg" :
                    path.endsWith(".png") ? "image/png" :
                    "text/plain";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}

```


## index.html

```

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Movie Recommender</title>
    <link rel="stylesheet" href="styles.css">
    <style>
        .page {
            display: none;
        }
        .page.active {
            display: block;
        }
        .page-nav {
            margin-top: 20px;
            display: flex;
            justify-content: space-between;
        }
        .page-nav button {
            width: auto;
            padding: 10px 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎬 Movie Recommender</h1>
        
        <!-- Mood Selection Page -->
        <div id="moodPage" class="preference-form page active">
            <h2>What's your current mood?</h2>
            
            <div class="mood-options">
                <button class="mood-btn" data-mood="happy">Happy 😊</button>
    <button class="mood-btn" data-mood="excited">Excited 🎉</button>
    <button class="mood-btn" data-mood="sad">Sad 😔</button>
    <button class="mood-btn" data-mood="tense">Tense 😨</button>
    <button class="mood-btn" data-mood="nostalgic">Nostalgic 🌅</button>
    <button class="mood-btn" data-mood="romantic">Romantic ❤️</button>
    <button class="mood-btn" data-mood="inspired">Inspired 🌟</button>
    <button class="mood-btn" data-mood="hopeful">Hopeful 🌈</button>
    <button class="mood-btn" data-mood="">Any Mood</button> 
                
            </div>
        </div>

        <!-- Pace Selection Page -->
        <div id="pacePage" class="preference-form page">
            <h2>What pace of movie are you looking for?</h2>
            
            <div class="pace-options">
                <button class="pace-btn" data-pace="fast">Fast 🚀</button>
                <button class="pace-btn" data-pace="medium">Medium 🌊</button>
                <button class="pace-btn" data-pace="slow">Slow 🌱</button>
                <button class="pace-btn" data-pace="">Any Pace</button>
            </div>

            <div class="page-nav">
                <button id="backToMood">Back</button>
            </div>
        </div>

        <!-- Family Friendly Selection Page -->
        <div id="familyFriendlyPage" class="preference-form page">
            <h2>Family Friendly?</h2>
            
            <div class="family-friendly-options">
                <button class="family-btn" data-family="true">Yes 👨‍👩‍👧‍👦</button>
                <button class="family-btn" data-family="false">No 🔞</button>
                <button class="family-btn" data-family="">Any</button>
            </div>

            <div class="page-nav">
                <button id="backToPace">Back</button>
            </div>
        </div>

        <!-- Recommendations Page -->
        <div id="recommendations" class="recommendations page">
            <!-- Recommended movies will be dynamically inserted here -->
            <div class="page-nav">
                <button id="restartSearch">Start Over</button>
            </div>
        </div>

        <div id="movieDetails" class="movie-details-container">
            <!-- Movie details will be dynamically inserted here -->
        </div>
    </div>

    <script>
        // Application state
        const moviePreferences = {
            mood: '',
            pace: '',
            familyFriendly: ''
        };

        // Page Navigation
        const pages = {
            mood: document.getElementById('moodPage'),
            pace: document.getElementById('pacePage'),
            familyFriendly: document.getElementById('familyFriendlyPage'),
            recommendations: document.getElementById('recommendations')
        };

        function showPage(pageToShow) {
            Object.values(pages).forEach(page => page.classList.remove('active'));
            pageToShow.classList.add('active');
        }

        // Mood Selection
        document.querySelectorAll('.mood-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                moviePreferences.mood = this.dataset.mood;
                showPage(pages.pace);
            });
        });

        // Pace Selection
        document.querySelectorAll('.pace-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                moviePreferences.pace = this.dataset.pace;
                showPage(pages.familyFriendly);
            });
        });

        // Family Friendly Selection
        document.querySelectorAll('.family-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                moviePreferences.familyFriendly = this.dataset.family;
                getRecommendations();
            });
        });

        // Back Navigation
        document.getElementById('backToMood').addEventListener('click', () => showPage(pages.mood));
        document.getElementById('backToPace').addEventListener('click', () => showPage(pages.pace));

        // Restart Search
        document.getElementById('restartSearch').addEventListener('click', () => {
            // Reset preferences
            moviePreferences.mood = '';
            moviePreferences.pace = '';
            moviePreferences.familyFriendly = '';
            
            // Clear recommendations
            pages.recommendations.innerHTML = '<div class="page-nav"><button id="restartSearch">Start Over</button></div>';
            
            // Go back to mood page
            showPage(pages.mood);
        });

        function getRecommendations() {
            // Construct query string
            let queryString = [];
            if (moviePreferences.mood) queryString.push(`mood=${moviePreferences.mood}`);
            if (moviePreferences.pace) queryString.push(`pace=${moviePreferences.pace}`);
            if (moviePreferences.familyFriendly) queryString.push(`familyFriendly=${moviePreferences.familyFriendly}`);

            fetch('/recommend', {
                method: 'POST',
                body: queryString.join('&'),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
            .then(response => response.json())
            .then(movies => {
                const recommendationsDiv = document.getElementById('recommendations');
                recommendationsDiv.innerHTML = '<h2>Recommended Movies</h2>';
                
                movies.forEach(movie => {
                    const movieDiv = document.createElement('div');
                    movieDiv.classList.add('movie-card');
                    
                    // Create a poster filename by replacing spaces with hyphens, removing special characters, and converting to lowercase
                    const posterFilename = movie.title
                        .replace(/[^a-zA-Z0-9\s]/g, '')  // Remove special characters
                        .replace(/\s+/g, '-')
                        .toLowerCase() + '.jpg';
                    
                    movieDiv.innerHTML = `
                        <h3>${movie.title}</h3>
                        <img src="posters/${posterFilename}" alt="${movie.title} Poster" 
                             onerror="this.onerror=null; this.src='posters/default-poster.jpg';" 
                             style="max-width: 200px; height: auto; margin-bottom: 10px;">
                        <p>Year: ${movie.year}</p>
                        <p>Rating: ⭐ ${movie.rating}</p>
                        <p>Genres: ${movie.genres}</p>
                        <button onclick="showMovieDetails('${movie.title}')">View Details</button>
                    `;
                    recommendationsDiv.appendChild(movieDiv);
                });

                // Add the "Start Over" button
                const startOverBtn = document.createElement('button');
                startOverBtn.id = 'restartSearch';
                startOverBtn.textContent = 'Start Over';
                startOverBtn.addEventListener('click', () => {
                    // Reset preferences
                    moviePreferences.mood = '';
                    moviePreferences.pace = '';
                    moviePreferences.familyFriendly = '';
                    
                    // Clear recommendations
                    recommendationsDiv.innerHTML = '';
                    
                    // Go back to mood page
                    showPage(pages.mood);
                });
                recommendationsDiv.appendChild(startOverBtn);

                showPage(pages.recommendations);
            });
        }

        function showMovieDetails(title) {
            fetch('/movie-details', {
                method: 'POST',
                body: title,
                headers: {
                    'Content-Type': 'text/plain'
                }
            })
            .then(response => response.text())
            .then(html => {
                document.getElementById('movieDetails').innerHTML = html;
                window.scrollTo({
                    top: document.getElementById('movieDetails').offsetTop,
                    behavior: 'smooth'
                });
            });
        }
    </script>
</body>
</html>

```


## style.css

```


:root {
    --netflix-black: #141414;
    --netflix-dark-gray: #1f1f1f;
    --netflix-red: #E50914;
    --netflix-gray: #5b5b5b;
    --netflix-light-gray: #8c8c8c;
    --netflix-white: #ffffff;
}

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Netflix Sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;
    background-color: var(--netflix-black);
    color: var(--netflix-white);
    line-height: 1.6;
    letter-spacing: -0.02em;
}

.container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 20px;
}

h1 {
    text-align: center;
    color: var(--netflix-red);
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 40px;
    text-transform: uppercase;
    letter-spacing: 2px;
}

.preference-form {
    background-color: var(--netflix-dark-gray);
    border-radius: 10px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
    padding: 40px;
    text-align: center;
    transition: all 0.3s ease;
}

.preference-form h2 {
    font-size: 1.8rem;
    margin-bottom: 30px;
    color: var(--netflix-white);
    font-weight: 500;
}

.mood-options, 
.pace-options, 
.family-friendly-options {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
}

.mood-btn, 
.pace-btn, 
.family-btn {
    background-color: var(--netflix-gray);
    border: none;
    color: var(--netflix-white);
    padding: 15px 25px;
    border-radius: 5px;
    font-size: 1.1rem;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    font-weight: 500;
}

.mood-btn:hover, 
.pace-btn:hover, 
.family-btn:hover {
    background-color: var(--netflix-red);
    transform: scale(1.05);
}

.page-nav {
    display: flex;
    justify-content: space-between;
    margin-top: 30px;
}

.page-nav button {
    background-color: var(--netflix-red);
    color: var(--netflix-white);
    border: none;
    padding: 12px 25px;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.3s ease;
}

.page-nav button:hover {
    background-color: #f6121d;
    transform: scale(1.05);
}

.recommendations {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 25px;
}

.movie-card {
    background-color: var(--netflix-dark-gray);
    border-radius: 10px;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.4);
    padding: 25px;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
}

.movie-card:hover {
    transform: scale(1.05);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.6);
}

.movie-card h3 {
    color: var(--netflix-white);
    margin-bottom: 15px;
    font-size: 1.3rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.movie-card button {
    margin-top: auto;
    background-color: var(--netflix-red);
    color: var(--netflix-white);
    border: none;
    padding: 10px 20px;
    border-radius: 5px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.movie-card button:hover {
    background-color: #f6121d;
}

.movie-details {
    background-color: var(--netflix-dark-gray);
    border-radius: 15px;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.5);
    padding: 40px;
    color: var(--netflix-light-gray);
}

.movie-details-content {
    display: flex;
    gap: 30px;
}

.movie-poster img {
    width: 100%;
    max-width: 300px;
    height: auto;
    object-fit: cover;
    border-radius: 10px;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5);
}

.trailer-btn {
    display: inline-block;
    background-color: var(--netflix-red);
    color: var(--netflix-white);
    padding: 12px 25px;
    text-decoration: none;
    border-radius: 5px;
    margin-top: 20px;
    transition: all 0.3s ease;
}

.trailer-btn:hover {
    background-color: #f6121d;
    transform: scale(1.05);
}

/* Responsive Adjustments */
@media (max-width: 768px) {
    .movie-details-content {
        flex-direction: column;
    }

    .movie-poster img {
        max-width: 100%;
    }

    .mood-options, 
    .pace-options, 
    .family-friendly-options {
        grid-template-columns: 1fr;
    }

    h1 {
        font-size: 2rem;
    }

    .preference-form {
        padding: 20px;
    }
}

/* Page Transition */
.page {
    opacity: 0;
    visibility: hidden;
    transform: translateY(20px);
    transition: all 0.4s ease;
}

.page.active {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
}

/* Scrollbar Styling for Netflix Theme */
::-webkit-scrollbar {
    width: 10px;
}

::-webkit-scrollbar-track {
    background: var(--netflix-dark-gray);
}

::-webkit-scrollbar-thumb {
    background: var(--netflix-red);
    border-radius: 5px;
}

::-webkit-scrollbar-thumb:hover {
    background: #f6121d;
}


```








