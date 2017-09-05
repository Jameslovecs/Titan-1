package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.ExternalAPI;
import external.ExternalAPIFactory;

public class MySQLConnection implements DBConnection {
	private static MySQLConnection instance;

	public static DBConnection getInstance() {
		if (instance == null) {
			instance = new MySQLConnection();
		}
		return instance;
	}

	// Import java.sql.Connection
	private Connection conn = null;

	private MySQLConnection() {
		try {
			// Forcing the class representing the MySQL driver to load and
			// initialize.
			// The newInstance() call is a work around for some broken Java
			// implementations.
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		String query = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				;
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		String query = "DELETE FROM history WHERE user_id=? AND item_id=? ";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				;
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteIds = new HashSet<>();
		try {
			String sql = "SELECT items.item_id from items, history "
					+ "WHERE items.item_id = history.item_id AND user_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				favoriteIds.add(rs.getString("item_id"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return favoriteIds;
	}

	// @Override
	// public Set<Item> getFavoriteItems(String userId) {
	// Set<Item> favorites = new HashSet<>();
	// try {
	// String sql = "SELECT * from items, history "
	// + "WHERE items.item_id = history.item_id AND user_id = ? ";
	// PreparedStatement statement = conn.prepareStatement(sql);
	// statement.setString(1, userId);
	// ResultSet rs = statement.executeQuery();
	// int i = 1;
	// while (rs.next()) {
	// Item item = new Item.ItemBuilder()
	// .setItemId(rs.getString("item_id"))
	// .setName(rs.getString("name"))
	// .setCity(rs.getString("city"))
	// .setState(rs.getString("state"))
	// .setCountry(rs.getString("country"))
	// .setZipcode(rs.getString("zipcode"))
	// .setRating(rs.getDouble("rating"))
	// .setAddress(rs.getString("address"))
	// .setLatitude(rs.getDouble("latitude"))
	// .setLongitude(rs.getDouble("longitude"))
	// .setDescription(rs.getString("description"))
	// .setSnippet(rs.getString("snippet"))
	// .setSnippetUrl(rs.getString("snippet_url"))
	// .setImageUrl(rs.getString("image_url"))
	// .setUrl(rs.getString("url"))
	// .build();
	// favorites.add(item);
	// }
	// } catch (Exception e) {
	// System.out.println(e.getMessage());
	// }
	// return favorites;
	// }

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<String> itemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		try {

			for (String itemId : itemIds) {
				String sql = "SELECT * from items WHERE item_id = ? ";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				ItemBuilder builder = new ItemBuilder();

				// Because itemId is unique and given one item id there should
				// have
				// only one result returned.
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setCity(rs.getString("city"));
					builder.setState(rs.getString("state"));
					builder.setCountry(rs.getString("country"));
					builder.setZipcode(rs.getString("zipcode"));
					builder.setRating(rs.getDouble("rating"));
					builder.setAddress(rs.getString("address"));
					builder.setLatitude(rs.getDouble("latitude"));
					builder.setLongitude(rs.getDouble("longitude"));
					builder.setDescription(rs.getString("description"));
					builder.setSnippet(rs.getString("snippet"));
					builder.setSnippetUrl(rs.getString("snippet_url"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setLocalDate(rs.getString("local_date"));
					builder.setLocalTime(rs.getString("local_time"));
				}

				// Join categories information into builder.
				// But why we do not join in sql? Because it'll be difficult
				// to set it in builder.
				sql = "SELECT * from categories WHERE item_id = ?";
				statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				rs = statement.executeQuery();
				Set<String> categories = new HashSet<>();
				while (rs.next()) {
					categories.add(rs.getString("category"));
				}
				builder.setCategories(categories);
				favoriteItems.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;

	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE item_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(String userId, double lat, double lon, String term) {
		// Connect to external API
		ExternalAPI api = ExternalAPIFactory.getExternalAPI(); // moved here
		List<Item> items = api.search(lat, lon, term);
		for (Item item : items) {
			// Save the item into our own db.
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		
		System.out.println("item local date: " + item.getLocalDate());
		System.out.println("item local time: " + item.getLocalTime());
		
		try {
			// First, insert into items table
			String sql = "INSERT IGNORE INTO items VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getCity());
			statement.setString(4, item.getState());
			statement.setString(5, item.getCountry());
			statement.setString(6, item.getZipcode());
			statement.setDouble(7, item.getRating());
			statement.setString(8, item.getAddress());
			statement.setDouble(9, item.getLatitude());
			statement.setDouble(10, item.getLongitude());
			statement.setString(11, item.getDescription());
			statement.setString(12, item.getSnippet());
			statement.setString(13, item.getSnippetUrl());
			statement.setString(14, item.getImageUrl());
			statement.setString(15, item.getUrl());
			statement.setString(16, item.getLocalTime());
			statement.setString(17, item.getLocalDate());

			statement.execute();

			// Second, update categories table for each category
			sql = "INSERT IGNORE INTO categories VALUES(?,?)";

			for (String category : item.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);

				statement.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
