import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    // Declare userRecommendations as a static or instance variable in the MovieRecommender class
private static Map<String, List<Movie>> userRecommendations = new HashMap<>();


    private static String[] parseCredentials(String json) {
        json = json.replaceAll("[{}\"]", "");
        Map<String, String> map = new HashMap<>();
        for (String pair : json.split(",")) {
            String[] keyValue = pair.split(":");
            map.put(keyValue[0], keyValue[1]);
        }
        return new String[]{map.get("username"), map.get("password")};
    }

    

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

            // Add these new endpoint handlers
server.createContext("/login", new HttpHandler() {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST,OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            String[] credentials = parseCredentials(requestBody.toString());
            String username = credentials[0];
            String password = credentials[1];
            
            if (DatabaseHandler.authenticateUser(username, password)) {
                String response = "{\"success\":true,\"message\":\"Login successful\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                String response = "{\"success\":false,\"message\":\"Invalid credentials\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(401, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST,OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
        }
    }
});

server.createContext("/register", new HttpHandler() {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST,OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            String[] credentials = parseCredentials(requestBody.toString());
            String username = credentials[0];
            String password = credentials[1];
            
            if (DatabaseHandler.userExists(username)) {
                String response = "{\"success\":false,\"message\":\"Username already exists\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                if (DatabaseHandler.registerUser(username, password)) {
                    String response = "{\"success\":true,\"message\":\"Registration successful\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    String response = "{\"success\":false,\"message\":\"Registration failed\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST,OPTIONS");
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
    


//javac MovieRecommender.java
//java MovieRecommender.java
//java -cp ".;sqlite-jdbc-3.42.0.0.jar" MovieRecommender