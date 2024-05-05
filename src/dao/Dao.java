package dao;

import java.sql.SQLException;
import java.util.ArrayList;

import model.Card;
import model.Player;

public interface Dao {
	
	public void connect() throws SQLException;
	
	public void disconnect() throws SQLException;
	
	public int getLastIdCard(int playerId) throws SQLException;
	
	public Card getLastCard() throws SQLException;
	
	public Player getPlayer(String user, String pass) throws SQLException;	
	
	public ArrayList<Card> getCards(int playerId) throws SQLException;
	
	public Card getCard(int cardId) throws SQLException;
	
	public void saveGame(Card card) throws SQLException;
	
	public void saveCard(Card card) throws SQLException;
	
	public void deleteCard(Card card) throws SQLException;
	
	public void clearDeck(int playerId) throws SQLException;
	
	public void addVictories(int playerId) throws SQLException;
	
	public void addGames(int playerId) throws SQLException;

}
