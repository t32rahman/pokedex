package io.github.t32rahman.pokedex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {
    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;
        PokedexViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            // Click Handler
            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Pokemon> pokemon = new ArrayList<>();
    private RequestQueue requestQueue;

    PokedexAdapter(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon();
    }

    public void loadPokemon() {
        String URL = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        pokemon.add(new Pokemon(
                                name.substring(0, 1).toUpperCase() + name.substring(1),
                                result.getString("url")
                        ));
                    }

                    notifyDataSetChanged();
                } catch (JSONException e){
                    Log.e("PokeAPI", "API Error");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PokeAPI", "API Response Error");
            }
        });

        requestQueue.add(request);
    }

    // Pokemon Filter
    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    private List<Pokemon> filtered = pokemon;

    private class PokemonFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<Pokemon> filteredPokemon = new ArrayList<>();
            int pokemon_list_length = pokemon.size();
            for (int i = 0; i < pokemon_list_length; i++) {
                int length = 0;
                int pokemon_name_length = pokemon.get(i).getName().length();

                if (constraint.length() <= pokemon_name_length) {
                    // Making sure index is within bounds
                    length = constraint.length();
                    if (pokemon.get(i).getName().substring(0, length).toLowerCase().equals(constraint.toString().toLowerCase())) {
                        // If pokemon.name substring is equal to user search input, then add it to filtered list
                        filteredPokemon.add(pokemon.get(i));
                    }
                }

            }

            results.values = filteredPokemon;
            results.count = filteredPokemon.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraints, FilterResults results) {
            filtered = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }
    // End Filter

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false);
        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = filtered.get(position);
        holder.textView.setText(current.getName());
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        try {
            int size = filtered.size();
            return size;
        } catch (Exception e) {
            return 0;
        }
    }
}
