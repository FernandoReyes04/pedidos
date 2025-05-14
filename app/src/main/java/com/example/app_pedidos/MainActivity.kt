package com.example.app_pedidos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var showDetailsButton: Button
    private lateinit var detailsCard: CardView
    private lateinit var resetLocationButton: Button
    private lateinit var cameraButton: ImageButton

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val LOCATION_REQUEST_CODE = 1001
    }

    // Para solicitar permisos de ubicación
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                getLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showDetailsButton = findViewById(R.id.showDetailsButton)
        detailsCard = findViewById(R.id.detailsCard)
        resetLocationButton = findViewById(R.id.resetLocationButton)
        cameraButton = findViewById(R.id.cameraButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        showDetailsButton.setOnClickListener {
            if (detailsCard.visibility == View.VISIBLE) {
                detailsCard.visibility = View.GONE
                showDetailsButton.text = "Ver detalles del pedido"
            } else {
                detailsCard.visibility = View.VISIBLE
                showDetailsButton.text = "Ocultar detalles"
            }
        }

        resetLocationButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent, LOCATION_REQUEST_CODE)
        }

        cameraButton.setOnClickListener {
            Toast.makeText(this, "Escaneo de paquete no implementado aún", Toast.LENGTH_SHORT).show()
        }

        showDetailsButton.setOnLongClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
            true
        }
    }

    private fun checkLocationPermissionsAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val message = "Ubicación: Lat ${it.latitude}, Lon ${it.longitude}"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val lat = data.getDoubleExtra("latitude", 0.0)
            val lng = data.getDoubleExtra("longitude", 0.0)

            // Usar Geocoder para obtener dirección legible
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Dirección desconocida"
                Toast.makeText(this, "Ubicación seleccionada:\n$address", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Ubicación: ($lat, $lng)", Toast.LENGTH_LONG).show()
            }
        }
    }
}
