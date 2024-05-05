package model;

public class Player {

    private int id;
    private String username;
    private String password;
    private String name;
    private int games;
    private int victories;
	
	public Player(String nombre) {
		this.name = nombre;
		this.games = 0;
		this.victories = 0;
	}
	
	public Player(int id, String name, int games, int victories) {
	    this.id = id;
	    this.name = name;
	    this.games = games;
	    this.victories = victories;
	}


    public Player(int id, String username, String password, String name, int games, int victories) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.games = games;
        this.victories = victories;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGames() {
		return games;
	}

	public void setGames(int games) {
		this.games = games;
	}

	public int getVictories() {
		return victories;
	}

	public void setVictories(int victories) {
		this.victories = victories;
	}
	
    public String getUsername() {
        return username;
    }
    

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Player [id=" + id + ", username=" + username + ", password=" + password + ", name=" + name + ", games=" + games + ", victories=" + victories + "]";
    }
}
