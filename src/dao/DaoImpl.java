package dao;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import model.Card;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DaoImpl {
	private static final String URL = "jdbc:mysql://localhost:3306/uno?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "DAM1T_M03";

    private Connection connection;

    public DaoImpl() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws SQLException {
        if (connection != null && connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    
    // Obtiene la info del jugador
    public Player getPlayer(String username, String password) throws SQLException {
        String query = "SELECT * FROM player WHERE user = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int games = resultSet.getInt("games");
                    int victories = resultSet.getInt("victories");
                    return new Player(id, name, games, victories);
                }
            }
        }
        return null;
    }


    // Obtiene todas las cartas
    public ArrayList<Card> getCards(int playerId) throws SQLException {
        ArrayList<Card> cards = new ArrayList<>();
        String query = "SELECT * FROM card WHERE id_player = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String number = resultSet.getString("number");
                    String color = resultSet.getString("color");
                    Card card = new Card(id, number, color, playerId);
                    cards.add(card);
                }
            }
        }
        return cards;
    }


    // Obtiene toda la infoo de la última carta
    public Card getLastCard() throws SQLException {
        String query = "SELECT * FROM card ORDER BY id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int playerId = resultSet.getInt("id_player");
                String number = resultSet.getString("number");
                String color = resultSet.getString("color");
                return new Card(id, number, color, playerId);
            }
        }
        return null; // Solo si no se encuentra la carta
    }


    // Obtiene la última ID de carta existente
    public int getLastIdCard(int playerId) throws SQLException {
        String query = "SELECT id FROM card WHERE id_player = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return 1; // Solo si no se encuentra la carta
    }


    // Almacena la carta en la base de datos
    public void saveCard(Card card) throws SQLException {
        String query = "INSERT INTO card (id_player, number, color) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, card.getPlayerId());
            statement.setString(2, card.getNumber());
            statement.setString(3, card.getColor());
            statement.executeUpdate();

            // Obtener la siguiente ID disponible
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                // Asignar la ID generada al objeto Card
                card.setId(generatedId);
            } else {
                throw new SQLException("No se pudo obtener la ID generada para la carta.");
            }
        }
    }

    // Borra la carta
    public void deleteCard(Card card) throws SQLException {
        String query = "DELETE FROM card WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();
        }
    }

    // Guarda partida
    public void saveGame(Card card) throws SQLException {
        int gameId = generateGameId(); // Obtiene la ID de partida
        String query = "INSERT INTO game (id_card, id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, card.getId());
            statement.setInt(2, gameId);
            statement.executeUpdate();
        }
    }
    
    // Obtiene la siguiente ID disponible en la tabla games
    private int generateGameId() throws SQLException {
        String query = "SELECT MAX(id) FROM game";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1; // Autoincrementa la ID
            }
        }
        return 1; // Devuelve 1 si es el primer juego
    }

    // Limpia el mazo
    public void clearDeck(int playerId) throws SQLException {
        String query = "DELETE FROM card WHERE id_player = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    // Suma al contador de victorias
    public void addVictories(int playerId) throws SQLException {
        String query = "UPDATE player SET victories = victories + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }
    
    // Suma al contador de partidas jugadas
    public void incrementGames(int playerId) throws SQLException {
        String query = "UPDATE player SET games = games + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }
    
    // Nueva partida
    public void addGames(int playerId) throws SQLException {
        String query = "UPDATE player SET games = games + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }
    
    // Guarda jugador en archivo .txt
    public void savePlayerDataToFile() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM player");
            BufferedWriter writer = new BufferedWriter(new FileWriter("playersFile.txt"));
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String user = resultSet.getString("user");
                String password = resultSet.getString("password");
                String name = resultSet.getString("name");
                int games = resultSet.getInt("games");
                int victories = resultSet.getInt("victories");
                String playerData = id + "," + user + "," + password + "," + name + "," + games + "," + victories;
                writer.write(playerData);
                writer.newLine();
            }
            writer.close();
            System.out.println("Datos de los jugadores guardados en el archivo playersFile.txt correctamente.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    // Guarda jugador en .xml usando DOM
    public void savePlayerDataToXMLWithDOM(Document doc, Element rootElement) throws SQLException {
        String query = "SELECT * FROM player";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String name = resultSet.getString("name");
                int games = resultSet.getInt("games");
                int victories = resultSet.getInt("victories");

                // Crear un elemento para cada jugador y agregarlo al documento XML
                Element playerElement = doc.createElement("player");
                rootElement.appendChild(playerElement);

                playerElement.setAttribute("id", Integer.toString(id));

                Element usernameElement = doc.createElement("username");
                usernameElement.appendChild(doc.createTextNode(username));
                playerElement.appendChild(usernameElement);

                Element passwordElement = doc.createElement("password");
                passwordElement.appendChild(doc.createTextNode(password));
                playerElement.appendChild(passwordElement);

                Element nameElement = doc.createElement("name");
                nameElement.appendChild(doc.createTextNode(name));
                playerElement.appendChild(nameElement);

                Element gamesElement = doc.createElement("games");
                gamesElement.appendChild(doc.createTextNode(Integer.toString(games)));
                playerElement.appendChild(gamesElement);

                Element victoriesElement = doc.createElement("victories");
                victoriesElement.appendChild(doc.createTextNode(Integer.toString(victories)));
                playerElement.appendChild(victoriesElement);
            }
        }
    }
    
    // Guarda jugador en .xml usando JAXB
    public void savePlayerDataToXMLWithJAXB() throws SQLException, JAXBException {
        // Conectarse si aún no lo ha hecho
        if (connection == null) {
            connect();
        }

        // Obtener la lista de jugadores desde la base de datos
        ArrayList<Player> players = getPlayerData();

        JAXBContext context = JAXBContext.newInstance(Player.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(players, new File("playersJAXB.xml"));
    }
    
    // Obtener info de un jugador
    public ArrayList<Player> getPlayerData() throws SQLException {
        ArrayList<Player> players = new ArrayList<>();
        String query = "SELECT * FROM player";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String name = resultSet.getString("name");
                int games = resultSet.getInt("games");
                int victories = resultSet.getInt("victories");
                Player player = new Player(id, username, password, name, games, victories); // Utiliza el constructor de Player
                players.add(player);
            }
        }
        return players;
    }
    
    // Limpiar la partida actual
    public void clearGame(int playerId) throws SQLException {
        String query = "DELETE FROM game WHERE id_card IN (SELECT id FROM card WHERE id_player = ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }


}
