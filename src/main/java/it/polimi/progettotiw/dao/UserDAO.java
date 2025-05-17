package it.polimi.progettotiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.progettotiw.beans.User;

public class UserDAO {
	private final Connection con; //session between a Java application and a database

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String user, String pwd) throws SQLException{
		//Don't need of password
		String query = "SELECT username FROM Users WHERE username = ? AND  password = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query)){
			pstatement.setString(1, user);
			pstatement.setString(2, pwd);
			try(ResultSet result = pstatement.executeQuery()){
				if(!result.isBeforeFirst()) //user is not logged
					return null;
				else {
					result.next();//only one user with that username and password
					User u = new User();
					u.setUsername(result.getString("username"));
					return u;
				}
			}
		}
	}

	public void registerUser(User user) throws SQLException{
		String query = "INSERT into Users (username, password, first_name,last_name) VALUES (?, ?, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query)){
			pstatement.setString(1, user.getUsername());
			pstatement.setString(2, user.getPassword());
			pstatement.setString(3, user.getName());
			pstatement.setString(4, user.getSurname());
			pstatement.executeUpdate();
		}
	}
	

	public ArrayList<User> findAllUsers() throws SQLException{
		String query = "SELECT username FROM Users ";
		try(PreparedStatement pstatement = con.prepareStatement(query)){
			try(ResultSet result = pstatement.executeQuery()){
				ArrayList<User> temp = new ArrayList<>();
				while(result.next()) {
					User u = new User();
					u.setUsername(result.getString("username"));
					temp.add(u);
				}
				return temp;
			}
		}
	}
}
