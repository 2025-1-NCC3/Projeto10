package br.com.fecapccp.ubergirls;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ItemLocalActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String endereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_local);

        // Recupera o endereço enviado pela tela anterior
        endereco = getIntent().getStringExtra("endereco");

        // Inicializa o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (endereco != null && !endereco.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(endereco, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title("Destino"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                } else {
                    Toast.makeText(this, "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao buscar localização", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
