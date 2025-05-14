package com.example.app_pedidos

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            // Mostrar modal de confirmación
            showSuccessDialog(result.contents)
        }
    }

    private fun showSuccessDialog(qrContent: String) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_success)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)

            findViewById<TextView>(R.id.tvSuccessMessage).text =
                "Paquete Confirmado\n"
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()

        }, 3000)
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
            checkCameraPermissionAndScan()
        }

        showDetailsButton.setOnLongClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
            true
        }
    }

    private fun checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startQRScanner()
        } else {
            // Solicitar permiso de cámara
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1002 // Código de solicitud para cámara
            )
        }
    }

    private fun startQRScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Escanea el código QR del paquete")
            setCameraId(0) // Usar cámara trasera
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        qrScanLauncher.launch(options)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1002 -> { // Código de solicitud para cámara
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startQRScanner()
                } else {
                    Toast.makeText(this, "Se necesita permiso de cámara para escanear QR", Toast.LENGTH_SHORT).show()
                }
            }
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