package com.example.finmate.speechtotext


import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finmate.components.saveExpenseToFirestoreVoice
import com.example.finmate.model.Expenses
import saveExpenseToFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToTextScreen(
    navController: NavController
) {
    var spokenText by remember { mutableStateOf("") }
    var extractedInfo by remember { mutableStateOf<Expenses?>(null) }

    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resultText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            resultText?.let {
                spokenText = it
                extractedInfo = parseSpeech(it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Speech to Expense",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your expense details")
                    }
                    speechLauncher.launch(intent)
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF12648D),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f),

                    ) {
                    Text("Speak Now")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Spoken Text:\n$spokenText", fontSize = 16.sp)

                Spacer(modifier = Modifier.height(20.dp))

                extractedInfo?.let { info ->
                    Text("🔍 Extracted Information:", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Title: ${info.title}")
                    Text("Amount: ₹${info.amount}")
                    Text("Category: ${info.category}")
                    Text("Date: ${info.date}")
                    Text("Time: ${info.time}")

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = {
                        saveExpenseToFirestoreVoice(
                            info,
                            onSuccess = {
                                Toast.makeText(context, "Expense Saved!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            },
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    )
}
val keywordCategoryMap = mapOf(
    // Food
    "domino" to "Food", "pizza" to "Food", "kfc" to "Food", "zomato" to "Food", "swiggy" to "Food",
    "Mc's" to "Food", "burger" to "Food", "food" to "Food",

    // Travel
    "ola" to "Transport", "uber" to "Transport", "flight" to "Transport", "airport" to "Transport", "train" to "Transport",
    // Shopping
    "flipkart" to "Shopping", "amazon" to "Shopping", "myntra" to "Shopping",
    // Health
    "hospital" to "Health", "doctor" to "Health", "medicine" to "Health", "clinic" to "Health",
    // Education
    "tuition" to "Education", "school" to "Education", "college" to "Education", "course" to "Education"
)