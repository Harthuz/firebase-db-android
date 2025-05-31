package com.example.firebase

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import com.example.firebase.ui.theme.FirebaseTheme
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegistrationScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegistrationScreen() {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var nome by rememberSaveable { mutableStateOf("") }
    var endereco by rememberSaveable { mutableStateOf("") }
    var bairro by rememberSaveable { mutableStateOf("") }
    var cep by rememberSaveable { mutableStateOf("") }
    var cidade by rememberSaveable { mutableStateOf("") }
    var estado by rememberSaveable { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var showUserData by remember { mutableStateOf(false) }
    var allUsersData by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    var dadosCadastrados by remember { mutableStateOf<Map<String, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize().safeContentPadding()
            .verticalScroll(rememberScrollState())

            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro",
            fontSize = 24.sp,
        )

        CriaInput(label = "Nome", valor = nome, onValueChange = { nome = it }, modifier = Modifier.focusRequester(focusRequester))

        Spacer(modifier = Modifier.height(16.dp))

        CriaInput(label = "Endereço", valor = endereco, onValueChange = { endereco = it })

        Spacer(modifier = Modifier.height(16.dp))

        CriaInput(label = "Bairro", valor = bairro, onValueChange = { bairro = it })

        Spacer(modifier = Modifier.height(16.dp))

        CriaInput(label = "CEP", valor = cep, onValueChange = { cep = it })

        Spacer(modifier = Modifier.height(16.dp))

        CriaInput(label = "Cidade", valor = cidade, onValueChange = { cidade = it })

        Spacer(modifier = Modifier.height(16.dp))

        CriaInput(label = "Estado", valor = estado, onValueChange = { estado = it })

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = {
                val db = Firebase.firestore

                if (nome.isBlank() || endereco.isBlank() || bairro.isBlank() || cep.isBlank() || cidade.isBlank() || estado.isBlank()) {
                    Log.w(TAG, "Campos vazios. Cadastro não realizado.")
                    showToast(context, "Por favor, preencha todos os campos.")

                    return@Button
                }

                val user = hashMapOf(
                    "nome" to nome,
                    "endereco" to endereco,
                    "bairro" to bairro,
                    "cep" to cep,
                    "cidade" to cidade,
                    "estado" to estado
                )

                // Add a new document with a generated ID
                db.collection("users")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        showToast(context, "Cadastro realizado com sucesso!")
                        Log.d(TAG, "Dados cadastrados com sucesso!")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao cadastrar dados", e)
                    }

                dadosCadastrados = user.mapValues { it.value.toString() }

                coroutineScope.launch {
                    nome = ""
                    endereco = ""
                    bairro = ""
                    cep = ""
                    estado = ""
                    cidade = ""
                }
                focusRequester.requestFocus()
                keyboardController?.show()
            }) {
                Text("Cadastrar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                coroutineScope.launch {
                    nome = ""
                    endereco = ""
                    bairro = ""
                    cep = ""
                    estado = ""
                    cidade = ""
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }) {
                Text("Cancelar")
            }
        }

        Row {
            Spacer(modifier = Modifier.width(20.dp))
            Button(onClick = {
                if (showUserData) {
                    showUserData = false
                } else {
                    val db = Firebase.firestore
                    db.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            allUsersData = result.map { document -> document.data }
                            showUserData = true
                            Log.d(TAG, "Dados de todos os usuários obtidos com sucesso!")
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Erro ao obter dados dos usuários.", exception)
                            showToast(context, "Erro ao buscar usuários.")
                        }
                }
            }) {
                Text(if (showUserData) "Fechar Lista" else "Listar Usuários")
            }
        }

        if (showUserData) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                allUsersData.forEach { userData ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text("Nome: ${userData["nome"]}")
                        Text("Endereço: ${userData["endereco"]}")
                        Text("Bairro: ${userData["bairro"]}")
                        Text("CEP: ${userData["cep"]}")
                        Text("Cidade: ${userData["cidade"]}")
                        Text("Estado: ${userData["estado"]}")
                    }
                }
            }
        }
    }
}

fun showToast(context: Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}

@Composable
fun CriaInput(label: String, valor: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp",
    showSystemUi = true
)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen()
}