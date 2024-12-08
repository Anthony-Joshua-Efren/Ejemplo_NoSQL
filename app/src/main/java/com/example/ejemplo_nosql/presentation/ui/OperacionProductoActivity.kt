package com.example.ejemplo_nosql.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.ejemplo_nosql.R
import com.example.ejemplo_nosql.data.model.ProductModel
import com.example.ejemplo_nosql.data.repository.ProductRepository
import com.example.ejemplo_nosql.databinding.ActivityOperacionProductoBinding
import com.example.ejemplo_nosql.presentation.common.UiState
import com.example.ejemplo_nosql.presentation.common.makeCall
import kotlinx.coroutines.launch
import pe.pcs.libpcs.UtilsCommon
import pe.pcs.libpcs.UtilsMessage

class OperacionProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOperacionProductoBinding
    private var _id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperacionProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        if(intent.extras != null)
            obtenerProducto()
    }

    private fun initListener() {
        binding.includeToolbar.toolbar.apply {
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            subtitle = "Registrar | Editar producto"
            navigationIcon = AppCompatResources.getDrawable(
                this@OperacionProductoActivity, R.drawable.baseline_arrow_back_24
            )
        }

        binding.includeToolbar.ibAccion.setImageResource(R.drawable.baseline_done_all_24)

        binding.includeToolbar.ibAccion.setOnClickListener {
            if (binding.etDescripcion.text.toString().trim().isEmpty() ||
                binding.etCodigoBarra.text.toString().trim().isEmpty() ||
                binding.etPrecio.text.toString().trim().isEmpty()
            ) {
                UtilsMessage.showAlertOk(
                    "ERROR", "Debe llenar todos los campos", this@OperacionProductoActivity
                )
                return@setOnClickListener
            }

            grabar(
                ProductModel(
                    id = _id,
                    descripcion = binding.etDescripcion.text.toString(),
                    codigobarra = binding.etCodigoBarra.text.toString(),
                    precio = binding.etPrecio.text.toString().toDouble()
                )
            )
        }
    }

    private fun obtenerProducto() {
        _id = intent.extras?.getString("id") ?: ""
        binding.etDescripcion.setText(intent.extras?.getString("descripcion"))
        binding.etCodigoBarra.setText(intent.extras?.getString("codigobarra"))
        binding.etPrecio.setText(intent.extras?.getDouble("precio").toString())
    }

    private fun grabar(model: ProductModel) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductRepository().grabar(model) }.let {
            when (it) {
                is UiState.Error -> {
                    binding.progressBar.isVisible = false
                    UtilsMessage.showAlertOk(
                        "ERROR", it.message, this@OperacionProductoActivity
                    )
                }

                is UiState.Success -> {
                    binding.progressBar.isVisible = false
                    UtilsMessage.showToast(this@OperacionProductoActivity, "Registro grabado")
                    UtilsCommon.cleanEditText(binding.root.rootView)
                    UtilsCommon.hideKeyboard(this@OperacionProductoActivity, binding.root.rootView)
                    binding.etDescripcion.requestFocus()
                    _id = ""
                    MainActivity.existeCambio = true
                }
            }
        }
    }
}