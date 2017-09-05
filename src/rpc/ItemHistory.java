package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBConnection conn = DBConnectionFactory.getDBConnection();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		Set<Item> favorites = conn.getFavoriteItems(userId);
		List<JSONObject> list = new ArrayList<>();
		try {
			for (Item item : favorites) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", true);
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = new JSONArray(list);
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			if (input.has("user_id") && input.has("favorite")) {
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("favorite");
				List<String> histories = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					String itemId = (String) array.get(i);
					histories.add(itemId);
				}
				conn.setFavoriteItems(userId, histories);
				RpcHelper.writeJsonObject(response, new JSONObject().put("status", "OK"));
			} else {
				RpcHelper.writeJsonObject(response, new JSONObject().put("status", "invalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			if (input.has("user_id") && input.has("favorite")) {
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("favorite");
				List<String> histories = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					String itemId = (String) array.get(i);
					histories.add(itemId);
				}
				conn.unsetFavoriteItems(userId, histories);
				RpcHelper.writeJsonObject(response, new JSONObject().put("status", "OK"));
			} else {
				RpcHelper.writeJsonObject(response, new JSONObject().put("status", "invalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
