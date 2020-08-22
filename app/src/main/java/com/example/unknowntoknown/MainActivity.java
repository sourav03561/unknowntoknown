package com.example.unknowntoknown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private arrayAdapter arrayAdapter;
    private int i;
    private FirebaseAuth mAuth;

    private String currentUId;
    private DatabaseReference usersDb;

     ListView listView;
     List<cards> rowItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        checkUserSex();

        rowItems = new ArrayList<cards>();


        arrayAdapter = new arrayAdapter(this, R.layout.item,  rowItems );

      SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                cards obj=(cards) dataObject;
                String userId=obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, "Left!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                cards obj=(cards) dataObject;
                String userId=obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Right!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "click",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb= usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Toast.makeText(MainActivity.this,"new Connection",Toast.LENGTH_SHORT).show();
                    String key=FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String userSex;
    private String oppositeUserSex;
    public void checkUserSex() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb =usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.child("sex").getValue()!=null)
                        {
                            userSex = dataSnapshot.child("sex").getValue().toString();

                            switch (userSex)
                            {
                                case "Male":
                                    oppositeUserSex = "Female";
                                    break;
                                case "Female":
                                    oppositeUserSex = "Male";
                                    break;
                            }
                            getoppositeSexUsers();
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
   public void getoppositeSexUsers(){

       usersDb.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
               if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex))
              {
                  String profileImageUrl="default";
                 if(!dataSnapshot.child("profileImageUrl").getValue().equals("default"))
                 {
                     profileImageUrl=dataSnapshot.child("profileImageUrl").getValue().toString();
                 }
                  cards Item=new cards(dataSnapshot.getKey(),dataSnapshot.child("name").getValue().toString(),profileImageUrl);
                  rowItems.add(Item);
                  arrayAdapter.notifyDataSetChanged();
              }
           }
           @Override
           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
           }
           @Override
           public void onChildRemoved(@NonNull DataSnapshot snapshot) {
           }
           @Override
           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
           }
           @Override
           public void onCancelled(@NonNull DatabaseError error) {
           }
       });
       //System.out.println(userSex);
   }
    public void LogoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }


    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }
}
