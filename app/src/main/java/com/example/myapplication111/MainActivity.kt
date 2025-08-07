package com.example.myapplication111

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONObject
import com.example.myapplication111.ui.theme.MyApplication111Theme
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.myapplication111.data.AppDatabase
import com.example.myapplication111.data.MessageEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField



data class Message(val sender: String, val content: String)

class MainActivity : ComponentActivity() {

    private val apiKey = ""
    private lateinit var db: AppDatabase
    private val messages = mutableStateListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "messages.db").build()
        // Load stored messages without blocking the UI thread
        lifecycleScope.launch {
            val stored = db.messageDao().getAll()
            messages.addAll(stored.map { Message(it.sender, it.content) })
        }
        setContent {
            MyApplication111Theme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen { showSplash = false }
                } else {
                    ChatScreen()
                }
            }
        }

    }

    private fun newChat() {
        messages.clear()
    }

    private fun clearHistory() {
        lifecycleScope.launch {
            db.messageDao().clearAll()
        }
        messages.clear()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen() {
        var userMessage by remember { mutableStateOf("") }
        val messages = this@MainActivity.messages
        val context = this

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "EPN Chat",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        Image(
                            painter = painterResource(R.drawable.buho),
                            contentDescription = "Owl icon",
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(32.dp),
                        )
                    },
                    actions = {
                        IconButton(onClick = { newChat() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { clearHistory() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages) { message ->
                        if (message.sender == "user") {
                            UserMessageBubble(message.content)
                        } else {
                            BotMessageBubble(message.content)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        placeholder = {
                            Text("Escribe tu mensaje" ,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                val content = userMessage
                                this@MainActivity.lifecycleScope.launch {
                                    db.messageDao().insert(
                                        MessageEntity(sender = "user", content = content, timestamp = System.currentTimeMillis())
                                    )
                                }
                                messages.add(Message("user", content))
                                callChatGPTAPI(context, content) { response ->
                                    val botReply = response ?: "No se recibió respuesta"
                                    this@MainActivity.lifecycleScope.launch {
                                        db.messageDao().insert(
                                            MessageEntity(sender = "bot", content = botReply, timestamp = System.currentTimeMillis())
                                        )
                                    }
                                    messages.add(Message("bot", botReply))
                                }
                                userMessage = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }

    @Composable
    fun UserMessageBubble(text: String) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun BotMessageBubble(fullText: String) {
        var visibleText by remember { mutableStateOf("") }
        LaunchedEffect(fullText) {
            visibleText = ""
            fullText.forEachIndexed { index, char ->
                delay(30)
                visibleText += char
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = visibleText,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    private fun callChatGPTAPI(context: ComponentActivity, mensaje: String, onResult: (String?) -> Unit) {
        val url = "https://api.openai.com/v1/chat/completions"
        val queue = Volley.newRequestQueue(context)

        val messagesJson = org.json.JSONArray()
        // Mensaje de sistema con instrucciones en español
        messagesJson.put(
            JSONObject().apply {
                put("role", "system")
                put("content", """
            Eres un asistente virtual experto en la Escuela Politécnica Nacional (EPN) de Quito. Tu objetivo es ayudar a los estudiantes con todo lo relacionado con trámites universitarios, fechas importantes, formatos oficiales, etc. Responde siempre con pasos claros y específicos para el contexto EPN, usando términos y enlaces oficiales.
        """.trimIndent())
            }
        )

        // Mensajes previos en memoria
        this.messages.forEach { msg ->
            val role = if (msg.sender == "user") "user" else "assistant"
            messagesJson.put(
                JSONObject().apply {
                    put("role", role)
                    put("content", msg.content)
                }
            )
        }

        // Nuevo mensaje del usuario
        messagesJson.put(
            JSONObject().apply {
                put("role", "user")
                put("content", mensaje)
            }
        )

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messagesJson)
            put("temperature", 0.7)
            put("max_tokens", 150)
        }

        val requestBody = jsonBody.toString()

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val message = firstChoice.getJSONObject("message")
                        val content = message.getString("content")
                        onResult(content)
                    } else {
                        onResult("No se encontró contenido en la respuesta.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult("Error al parsear la respuesta: ${e.message}")
                }
            },
            { error ->
                error.printStackTrace()
                onResult("Error en la petición: ${error.message}")
            }) {

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                val parsed = response?.data?.let { String(it, Charsets.UTF_8) } ?: ""
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            }

            override fun getBodyContentType(): String = "application/json"

            override fun getBody(): ByteArray = requestBody.toByteArray(Charsets.UTF_8)

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )
            }
        }

        queue.add(stringRequest)
    }
}
