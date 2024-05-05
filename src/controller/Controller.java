package controller;

import dao.DaoImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import model.Card;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utils.Color;
import utils.Number;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {
    private static Controller controller;
    private DaoImpl dao;
    private Player player;
    private ArrayList<Card> cards;
    private Scanner s;
    private Card lastCard;

    public Controller() {
        dao = new DaoImpl();
        s = new Scanner(System.in);
    }
    
    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }
    
    public void savePlayerDataToFile() {
        dao.savePlayerDataToFile();
    }

    public void init() {
        try {
            dao.connect();

            if (loginUser()) {
                startGame();
                playTurn();
                endGame();
            } else {
                System.out.println("Usuario o contraseña incorrecta.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                dao.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void playTurn() throws SQLException {
        Card card = null;
        boolean correctCard = false;
        boolean end = false;

        while (!end) {
            do {
                showCards();
                System.out.println("Pulsa -1 para robar una carta.");
                System.out.println("Pulsa -2 para salir del juego.");
                System.out.println("Pulsa -3 para acceder al submenu.");
                int position = s.nextInt();

                switch (position) {
                case -1:
                	drawCards(1);
                	break;
                case -2:
                	correctCard = true;
                	end = true;
                	System.out.println("Saliendo del juego...");
                	break;
                case -3:
                    boolean subMenuEnd = false;
                    while (!subMenuEnd) {
                        System.out.println("Submenú:");
                        System.out.println("Pulsa F(ile) para guardar datos del jugador en archivo.");
                        System.out.println("Pulsa D(OM) para guardar los datos del jugador en XML usando la librería DOM.");
                        System.out.println("Pulsa J(AXB) para guardar los datos del jugador en XML usando la librería JAXB.");
                        System.out.println("Pulsa B(ack) para volver al menú principal.");

                        char subOption = s.next().charAt(0);
                        switch (subOption) {
                            case 'F':
                            case 'f':
                                // Guardar en archivo TXT
                                savePlayerDataToFile();
                                break;
                            case 'D':
                            case 'd':
                                // Guardar en XML usando DOM
                                savePlayerDataToXMLWithDOM();
                                break;
                            case 'J':
                            case 'j':
                                // Guardar en XML usando JAXB
                                savePlayerDataToXMLWithJAXB();
                                break;
                            case 'B':
                            case 'b':
                                // Salir del submenú
                                subMenuEnd = true;
                                break;
                            default:
                                System.out.println("ERROR: Opción inválida.");
                                break;
                        }
                    }
                    break;
                    
                default:
                    card = selectCard(position);
                    correctCard = validateCard(card);
                    
                    if (correctCard) {
                        if (card.getNumber().equalsIgnoreCase(Number.SKIP.toString()) || card.getNumber().equalsIgnoreCase(Number.CHANGESIDE.toString())) {
                            this.cards.remove(card);
                            dao.deleteCard(card);
                            end = true;
                            System.out.println("La partida ha terminado, saliendo...");
                            break;
                        }
                    }

                    if (correctCard && !end) {
                        System.out.println("Ok. Siguiente turno");
                        lastCard = card;
                        dao.saveGame(card);
                        this.cards.remove(card);
                    } else {
                        System.out.println("Esta carta no puede ser jugada. Usa otra o roba carta");
                    }
                    break;
                }
            } while (!correctCard);
            
            if (this.cards.size() == 0) {
            	endGame();
            	end = true;
            	System.out.println("Sin cartas... ¡HAS GANADO!");
            	break;
            }
        }
    }


	private boolean validateCard(Card card) {
		if (lastCard != null) {
			// Si es del mismo color
			if (lastCard.getColor().equalsIgnoreCase(card.getColor())) return true;
			// Si es del mismo numero
			if (lastCard.getNumber().equalsIgnoreCase(card.getNumber())) return true;
			// Si la anterior es negra, ignora las validaciones
			if (lastCard.getColor().equalsIgnoreCase(Color.BLACK.name())) return true;
			// Si es de color negro
			if (card.getColor().equalsIgnoreCase(Color.BLACK.name())) return true;
			
			return false;
		} else {
			return true;
		}
	}

	private void endGame() throws SQLException {
	    // Eliminar las partidas asociadas a las cartas del jugador
	    dao.clearGame(player.getId());
	    // Eliminar las cartas asociadas al jugador
	    dao.clearDeck(player.getId());
	    // Incrementar las victorias y los juegos del jugador
	    dao.addVictories(player.getId());
	    dao.incrementGames(player.getId());
	}

	private Card selectCard(int id) {
		Card card = this.cards.get(id);
		return card;
	}

	private void showCards() {
		System.out.println("================================================");
		if (null == lastCard) {
			System.out.println("Primera partida. Sin cartas en mesa");
		} else {
			System.out.println("La carta en mesa es " + lastCard.toString());
		}
		System.out.println("================================================");
		System.out.println("Tienes " + cards.size() + " cartas en mano: ");
		for (int i = 0; i < cards.size(); i++) {
			System.out.println(i + "." + cards.get(i).toString());
		}
	}

    private boolean loginUser() throws SQLException {
        System.out.println("Bienvenido al juego UNO!!");
        System.out.println("Nombre de usuario: ");
        String user = s.next();
        System.out.println("Contraseña: ");
        String pass = s.next();

        player = dao.getPlayer(user, pass);

        return player != null;
    }
	
	
	private void startGame() throws SQLException {
        cards = dao.getCards(player.getId());

        if (cards.size() == 0) {
            drawCards(3);
        }

        lastCard = dao.getLastCard();
        if (lastCard != null && (lastCard.getNumber().equalsIgnoreCase(Number.TWOMORE.toString()) || lastCard.getNumber().equalsIgnoreCase(Number.FOURMORE.toString()))) {
        	drawCards(lastCard.getNumber().equalsIgnoreCase(Number.TWOMORE.toString()) ? 2 : 4);
        }
    }
	
	private void drawCards(int numberCards) throws SQLException {
		
		for (int i = 0; i < numberCards; i++) {
			int id = dao.getLastIdCard(player.getId());
			
			String number = Number.getRandomCard();
			String color="";
			if (number.equalsIgnoreCase(Number.WILD.toString())|| number.equalsIgnoreCase(Number.FOURMORE.toString())){
				color = Color.BLACK.toString();
			}else {
				color = Color.getRandomColor();
			}
					
			Card c = new Card(id, number, color, player.getId());
			dao.saveCard(c);
			cards.add(c);
		}
	}
	
	public void savePlayerDataToXMLWithDOM() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Crear el documento XML
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("players");
            doc.appendChild(rootElement);

            // Obtener datos de los jugadores desde la base de datos
            ArrayList<Player> players = dao.getPlayerData();
            for (Player player : players) {
                Element playerElement = doc.createElement("player");
                playerElement.setAttribute("id", String.valueOf(player.getId()));
                playerElement.appendChild(createPlayerElement(doc, "username", player.getUsername()));
                playerElement.appendChild(createPlayerElement(doc, "password", player.getPassword()));
                playerElement.appendChild(createPlayerElement(doc, "name", player.getName()));
                playerElement.appendChild(createPlayerElement(doc, "games", String.valueOf(player.getGames())));
                playerElement.appendChild(createPlayerElement(doc, "victories", String.valueOf(player.getVictories())));
                rootElement.appendChild(playerElement);
            }

            // Escribir el contenido del documento XML en un archivo
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("playersDOM.xml"));
            transformer.transform(source, result);

            System.out.println("Archivo XML generado correctamente.");

        } catch (ParserConfigurationException | TransformerException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Element createPlayerElement(Document doc, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        return element;
    }

    
    public void savePlayerDataToXMLWithJAXB() {
        try {
            // Obtener datos de los jugadores desde la base de datos
            List<Player> players = dao.getPlayerData();

            // Crear un contexto JAXB para la clase Player
            JAXBContext context = JAXBContext.newInstance(Player.class);

            // Crear un marshaller
            Marshaller marshaller = context.createMarshaller();

            // Formatear la salida XML
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Crear un archivo de salida
            File file = new File("playersJAXB.xml");

            // Marshalling y escribir el XML al archivo
            marshaller.marshal(players, new FileOutputStream(file));

            System.out.println("Datos de jugadores guardados en el archivo playersJAXB.xml con éxito.");
        } catch (JAXBException | FileNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Error al guardar los datos de los jugadores en el archivo XML con JAXB.");
        }
    }
}
