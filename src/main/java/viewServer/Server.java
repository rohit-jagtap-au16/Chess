package viewServer;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import controller.DualViewChessControl;
import controller.ViewController;
import javax.annotation.Nullable;

public class Server {

  private DatabaseReference ref;
  private RootListener rootListener;
  private Map<String, ViewController> games = new HashMap<>();

  public Server(FirebaseDatabase data) {
    this.ref = data.getReference();
    this.rootListener = new RootListener();
    FirestoreClient.getFirestore().collection("rooms").addSnapshotListener(rootListener);
    System.err.println("Listener added");
  }

  private class RootListener implements ChildEventListener, EventListener<QuerySnapshot> {

    @Override
    public void onCancelled(DatabaseError arg0) {
      throw new RuntimeException();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException error) {
      if (error != null) {
        System.err.println("Listen failed: " + error);
        return;
      }
      for (DocumentChange dc : snapshots.getDocumentChanges()) {
        switch (dc.getType()) {
          case ADDED:
            final QueryDocumentSnapshot document = dc.getDocument();
            final Map<String, Object> data = document.getData();
            String roomLoc = document.getId();
            ServerChessView whiteview = ServerChessView
                .newInstance(FirestoreClient.getFirestore().collection("rooms").document(roomLoc)
                    .collection("display")
                    .document("white"), (String) data.get("owner"), true);
            ServerChessView blackview = ServerChessView
                .newInstance(FirestoreClient.getFirestore().collection("rooms").document(roomLoc)
                    .collection("display")
                    .document("black"), (String) data.get("invite") , false);
            System.out.println("Room " + data.toString() + " added");
            games.put(roomLoc, new DualViewChessControl(whiteview, blackview));
            FirestoreClient.getFirestore().collection("rooms").document(roomLoc)
                .update("$key", roomLoc);
            break;
          default:
            break;
        }
      }
    }

    @Override
    public void onChildAdded(DataSnapshot data, String arg1) {
    }

    @Override
    public void onChildChanged(DataSnapshot arg0, String arg1) {
    }

    @Override
    public void onChildMoved(DataSnapshot arg0, String arg1) {
    }

    @Override
    public void onChildRemoved(DataSnapshot data) {
      String roomLoc = data.getKey();

      ViewController viewControl = games.remove(roomLoc);
      viewControl.close();
    }
  }

  public Map<String, ViewController> getGames() {
    return games;
  }
}
