package io.github.t32rahman.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {

    private ImageView pokemonImageView;

    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView descriptionTextView;

    private String URL;
    public RequestQueue requestQueue;


    public String pokemon_text_url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        URL = getIntent().getStringExtra("url");

        pokemonImageView = findViewById(R.id.pokemon_image);

        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        descriptionTextView = findViewById(R.id.pokemon_description);

        load();
    }


    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Flavor text
                    descriptionTextView.setText("");
                    pokemon_text_url = "https://pokeapi.co/api/v2/pokemon-species/" + response.getInt("id") + "/";
                    JsonObjectRequest request_text = new JsonObjectRequest(Request.Method.GET, pokemon_text_url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray flavor_text_entries = response.getJSONArray("flavor_text_entries");
                                for (int i = 0; i < flavor_text_entries.length(); i++) {
                                    String lang = flavor_text_entries.getJSONObject(i).getJSONObject("language").getString("name");
                                    if (lang.toLowerCase().equals("en")) {
                                        String flavor_text = flavor_text_entries.getJSONObject(0).getString("flavor_text");
                                        descriptionTextView.setText(flavor_text);
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("PokeAPI", "API Description call error");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PokeAPI", pokemon_text_url);
                            error.getCause();
                        }
                    });
                    requestQueue.add(request_text);
                    // End Flavor text

                    // Download and display image
                    String image_url = response.getJSONObject("sprites").getString("front_default");
                    new SpriteDownloader().execute(image_url);

                    // Update Name
                    String name = response.getString("name");
                    nameTextView.setText(
                            name.substring(0,1).toUpperCase() + name.substring(1)
                    );

                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                    JSONObject species = response.getJSONObject("species");
                    pokemon_text_url = species.getString("url");

                } catch (JSONException e){
                    Log.e("PokeAPI", "Pokemon json Error");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PokeAPI", "Pokemon details Error");
            }
        });

        requestQueue.add(request);
    }

    // Used to download sprite
    private class SpriteDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap pokemonImage) {
            // load the bitmap into the ImageView!
            pokemonImageView.setImageBitmap(pokemonImage);
        }
    }
}
