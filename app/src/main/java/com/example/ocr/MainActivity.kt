package com.example.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var editText: EditText

    private val listaDeMarcas = listOf(
        "ama", "ambev", "brahma", "budweiser", "stella", "artois", "beck's", "becks", "beck",
        "skol", "wälls", "walls", "xwalls", "x-walls", "spaten", "bohemia", "colorado",
        "corona", "beats", "antartica", "serramalte"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        editText = findViewById(R.id.editText)
    }


    fun pickImage(v: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecionar Imagem"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageView.setImageURI(data!!.data)

        }
    }

    fun processImgage(v: View) {
        if (imageView.drawable != null) {
            editText.setText("")
            v.isEnabled = false
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    v.isEnabled = true
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {
                    v.isEnabled = true
                    editText.setText("Falha")
                }
        } else {
            Toast.makeText(this, "Selecione uma imagem primeiro", Toast.LENGTH_LONG).show()
        }

    }


    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            editText.setText("Nenhum texto encontrado")
            return
        }
        for (block in resultText.textBlocks) {
            val blockText = block.text
            showMatches(blockText.toLowerCase())
           // editText.append("$blockText\n")
        }
    }

    private fun showMatches(identificado: String) {
        if (listaDeMarcas.contains(identificado))
            editText.append(identificado.toUpperCase() + "\n")
        else {
            listaDeMarcas.forEach { marca ->
                if (identificado.count() >= 3 && marca.startsWith(identificado.substring(0, 3)))
                    editText.append("Marca: $marca->$identificado\n")
            }
        }
    }
}
