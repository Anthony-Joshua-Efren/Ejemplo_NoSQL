package com.example.ejemplo_nosql.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplo_nosql.data.model.ProductModel
import com.example.ejemplo_nosql.data.repository.ProductRepository
import com.example.ejemplo_nosql.databinding.ActivityMainBinding
import com.example.ejemplo_nosql.presentation.adapter.ProductAdapter
import com.example.ejemplo_nosql.presentation.common.UiState
import com.example.ejemplo_nosql.presentation.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import pe.pcs.libpcs.UtilsCommon
import pe.pcs.libpcs.UtilsMessage

class MainActivity : AppCompatActivity(), ProductAdapter.IOnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        leerProducto("")
    }

    override fun onResume() {
        super.onResume()

        if(!existeCambio) return

        existeCambio = false
        leerProducto(binding.etBuscar.text.toString().trim())
    }

    private fun initListener() {

        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        binding.rvLista.apply {
            adapter = ProductAdapter(this@MainActivity)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.tilBuscar.setEndIconOnClickListener {
            leerProducto(binding.etBuscar.text.toString().trim())
            UtilsCommon.hideKeyboard(this@MainActivity, it)
        }

    }

    private fun leerProducto(dato: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductRepository().listar(dato) }.let {
            when (it) {
                is UiState.Error -> {
                    binding.progressBar.isVisible = false
                    UtilsMessage.showAlertOk(
                        "ERROR", it.message, this@MainActivity
                    )
                }

                is UiState.Success -> {
                    binding.progressBar.isVisible = false
                    (binding.rvLista.adapter as ProductAdapter).setLista(it.data)
                }
            }
        }
    }

    private fun eliminar(model: ProductModel) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductRepository().eliminar(model) }.let {
            when (it) {
                is UiState.Error -> {
                    binding.progressBar.isVisible = false
                    UtilsMessage.showAlertOk(
                        "ERROR", it.message, this@MainActivity
                    )
                }

                is UiState.Success -> {
                    binding.progressBar.isVisible = false
                    UtilsMessage.showToast(this@MainActivity, "Registro eliminado")
                    leerProducto(binding.etBuscar.text.toString().trim())
                }
            }
        }
    }

    override fun clickEditar(productModel: ProductModel) {
        startActivity(
            Intent(this, OperacionProductoActivity::class.java).apply {
                putExtra("id", productModel.id)
                putExtra("descripcion", productModel.descripcion)
                putExtra("codigobarra", productModel.codigobarra)
                putExtra("precio", productModel.precio)
            }
        )
    }

    override fun clickEliminar(productModel: ProductModel) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Eliminar")
            setMessage("¿Desea eliminar el registro ${productModel.descripcion}?")
            setCancelable(false)

            setPositiveButton("SI") { dialog, _ ->
                eliminar(productModel)
                leerProducto(binding.etBuscar.text.toString().trim())
                dialog.dismiss()
            }

            setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }

    companion object {
        var existeCambio = false
    }
}