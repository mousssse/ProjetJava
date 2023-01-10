package main.java.com.view;

import java.sql.SQLException;
import java.time.LocalDateTime;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.com.controller.DBManager;
import main.java.com.controller.ListenerManager;
import main.java.com.controller.OnlineUsersManager;
import main.java.com.controller.listener.ChatListener;
import main.java.com.controller.listener.UsernameListener;
import main.java.com.model.Message;
import main.java.com.model.MessageType;
import main.java.com.model.User;

/**
 * @author sarah
 * @author Sandro
 *
 */
public class ChatStage extends Stage implements ChatListener, UsernameListener {
	private User remoteUser;
	private boolean isOnline;
	private ObservableList<Message> vector = FXCollections.observableArrayList();
	private ListView<Message> messageList = new ListView<>();
	private BorderPane rootPane = new BorderPane();
	private Label inputLabel = new Label("Send message: ");
	private TextField inputField = new TextField();
	
	public ChatStage(User remoteUser, Boolean online) {
		this.remoteUser = remoteUser;
		this.isOnline = online;
		this.updateMessageVector();
        this.messageList.setItems(this.vector);
        
        this.messageList.setCellFactory(param -> new ListCell<Message>(){
        	@Override
        	public void updateItem(Message message, boolean empty) {
        	    super.updateItem(message, empty);
        	    setText(null);

        	    if (message != null) {
        	        // Manage the text width
        	        Text text = new Text(message.toString());
        	        text.wrappingWidthProperty().bind(getListView().widthProperty().subtract(20));
        	        setGraphic(text);
        	    }
        	    else {
        	        setGraphic(null);
        	    }
        	}
        });
        
    	ScrollPane messagePane = new ScrollPane(this.messageList);
    	messagePane.setFitToWidth(true);
    	messagePane.setFitToHeight(true);
    	messagePane.setHbarPolicy(ScrollBarPolicy.NEVER);

		VBox inputBox = new VBox();
		inputBox.setPadding(new Insets(8));
        if (this.isOnline) {
    		HBox hbox = new HBox(this.inputLabel, this.inputField);
    		HBox.setHgrow(this.inputField, Priority.ALWAYS);
    		hbox.setAlignment(Pos.CENTER);
    		inputBox.getChildren().add(hbox);
        }
        else {
        	inputBox.getChildren().add(new Label(remoteUser.getUsername() + " is offline. You can't send them messages."));
        }
		
        inputField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String messageContent = inputField.getText().trim();
                // The empty string is not allowed.
                if (!messageContent.isEmpty()) {
    				ListenerManager.getInstance().fireOnMessageToSend(OnlineUsersManager.getInstance().getLocalUser(), remoteUser, messageContent, LocalDateTime.now(), MessageType.MESSAGE);
    				// Resetting the text field.
    				inputField.setText(null);
                }
            }
        });
        
        this.rootPane.setCenter(messagePane);
        this.rootPane.setBottom(inputBox);
        Scene scene = new Scene(this.rootPane, 500, 500);
        //scene.getStylesheets().add("path to CSS file");
        this.setMinWidth(400);
        this.setMinHeight(300);
        this.setScene(scene);
        this.setTitle("Conversation with " + this.remoteUser.getUsername());
        
        this.setOnCloseRequest(new EventHandler<WindowEvent>() {
        	@Override
        	public void handle(WindowEvent e) {
        		if (isOnline) ListenerManager.getInstance().fireOnMessageToSend(OnlineUsersManager.getInstance().getLocalUser(), remoteUser, null, LocalDateTime.now(), MessageType.CLOSING_CONVERSATION);
        		getStage().close();
        	}
        });
        
        this.show();
	}
    
    
    private Stage getStage() {
		return this;
	}
    
    private void updateMessageVector() {
    	this.vector.clear();
    	try {
    		vector.addAll(DBManager.getInstance().getConversationHistory(this.remoteUser.getId()));
		} catch (SQLException e) {
			//Display an error message if the database could not retrieve history
			Alert historyNotLoaded = new Alert(AlertType.NONE);
			historyNotLoaded.getDialogPane().getButtonTypes().add(ButtonType.OK);
			historyNotLoaded.setTitle("History error");
			historyNotLoaded.setContentText("Could not load conversation history with " + this.remoteUser.getUsername());
			historyNotLoaded.showAndWait();
		}
    }

	@Override
	public void onUsernameModification(User user, String newUsername) {
		user.setUsername(newUsername);
		this.remoteUser = user;
		Platform.runLater(() -> this.setTitle("Conversation with " + newUsername));
		Platform.runLater(() -> this.updateMessageVector());
	}

	@Override
	public void onSelfUsernameModification(String newUsername) {
		this.vector.clear();
		this.updateMessageVector();
	}

	@Override
	public void onChatRequest(User remoteUser) {
		// TODO automatically open the chat frame? create an accept / deny mechanism?
		//ChatFrame newFrame = new ChatFrame(remoteUser);
	}

	@Override
	public void onChatClosure(User remoteUser) {
		Platform.runLater(() -> this.close());
	}

	@Override
	public void onMessageToSend(User localUser, User remoteUser, String messageContent, LocalDateTime date,	MessageType type) {
		Platform.runLater(() -> vector.add(new Message(localUser, remoteUser, messageContent, date, type)));
	}

	@Override
	public void onMessageToReceive(Message message) {
		Platform.runLater(() -> vector.add(message));
	}
}
