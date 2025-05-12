package com.example.app_pedidos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var updateLocationButton: Button

    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtener el fragmento del mapa y inicializarlo
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botón para actualizar la ubicación seleccionada
        updateLocationButton = findViewById(R.id.updateLocationButton)
        updateLocationButton.visibility = View.GONE // Inicialmente oculto

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Mostrar mensaje con ícono al entrar
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()

        // Solicitar permisos de ubicación si no están dados
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        mMap.isMyLocationEnabled = true

        // Obtener ubicación actual y mover la camara ahi
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }

        // Permitir al usuario seleccionar ubicación con un clic normal
        mMap.setOnMapClickListener { latLng ->
            mMap.clear() // Limpiar el mapa antes de agregar el nuevo marcador
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            selectedLatLng = latLng
            updateLocationButton.visibility = View.VISIBLE // Mostrar el botón para actualizar
        }

        // Acción del botón "Actualizar ubicación"
        updateLocationButton.setOnClickListener {
            selectedLatLng?.let {
                // Devolver la ubicación seleccionada
                val intent = Intent().apply {
                    putExtra("latitude", it.latitude)
                    putExtra("longitude", it.longitude)
                }
                setResult(RESULT_OK, intent)
                finish() // Finalizar la actividad
            } ?: Toast.makeText(this, "Selecciona una ubicación primero", Toast.LENGTH_SHORT).show()
        }
    }
}
