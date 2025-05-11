package com.example.app_pedidos

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var showDetailsButton: Button
    private lateinit var detailsCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los elementos
        showDetailsButton = findViewById(R.id.showDetailsButton)
        detailsCard = findViewById(R.id.detailsCard)

        // Acción: mostrar/ocultar el CardView
        showDetailsButton.setOnClickListener {
            if (detailsCard.visibility == View.VISIBLE) {
                detailsCard.visibility = View.GONE
                showDetailsButton.text = "Ver detalles del pedido"
            } else {
                detailsCard.visibility = View.VISIBLE
                showDetailsButton.text = "Ocultar detalles"
            }
        }
    }
}
