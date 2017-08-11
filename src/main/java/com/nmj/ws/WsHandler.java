package com.nmj.ws;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

@WebSocket
public class WsHandler {

	private static final int MAX_COUNT = 5;
	private static final int INITIAL_WAIT_TIME = 1000;
	private static final int POLLING_INTERVAL = 2000;
	
	static final Set<Session> sessions = new CopyOnWriteArraySet<>();

	@OnWebSocketConnect
	public void onConnect(Session session) throws Exception {
		sessions.add(session);
		startService();
	}

	private void startService() {
		Timer timer = new Timer("myTimer", true);
		timer.scheduleAtFixedRate(new TimerTask() {
			private int counter = 0;

			@Override
			public void run() {
				broadcastMessage();
				counter++;

				if (counter >= MAX_COUNT) {
					timer.cancel();
					stopService();
				}

			}

		}, INITIAL_WAIT_TIME, POLLING_INTERVAL);

	}

	private void stopService() {
		sessions.stream().forEach(Session::close);
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		sessions.remove(user);
	}

	private static void broadcastMessage() {
		sessions.stream()//
				.filter(Session::isOpen) //
				.forEach(session -> {
					try {
						session.getRemote().sendString(//
								String.valueOf(new JSONObject()//
										.put("data", UUID.randomUUID())));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
	}
}
