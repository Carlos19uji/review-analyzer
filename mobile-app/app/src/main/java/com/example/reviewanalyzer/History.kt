package com.example.reviewanalyzer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalysisItem(
    val docId: String,
    val timestamp: Long,
    val word: String,
    val commonWords: Map<String, Int>,
    val analysisResults: List<ReviewData>
)

@Composable
fun History(navController: NavController, auth: FirebaseAuth) {
    var analysisHistory by remember { mutableStateOf<List<AnalysisItem>>(emptyList()) }
    var loadingState by remember { mutableStateOf(true) } // Estado de carga
    val db = FirebaseFirestore.getInstance()
    val userID = auth.currentUser?.uid ?: return

    // Cargar datos desde Firestore
    LaunchedEffect(userID) {
        db.collection("users").document(userID)
            .collection("analysis").get()
            .addOnSuccessListener { result ->
                val historyList = result.documents.mapNotNull { doc ->
                    val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                    val word = doc.getString("word") ?: "Unknown"
                    val commonWords = doc.get("common_words") as? Map<String, Long> ?: emptyMap()
                    val analysisResults = (doc.get("analysis_results") as? List<Map<String, Any>>)
                        ?.mapNotNull { review ->
                            val text = review["text"] as? String ?: return@mapNotNull null
                            val sentiment = review["sentiment"] as? String ?: return@mapNotNull null
                            val emotion = review["emotion"] as? String ?: return@mapNotNull null
                            ReviewData(text, sentiment, emotion)
                        } ?: emptyList()

                    AnalysisItem(
                        docId = doc.id,
                        timestamp = timestamp,
                        word = word,
                        commonWords = commonWords.mapValues { it.value.toInt() },
                        analysisResults = analysisResults
                    )
                }.sortedByDescending { it.timestamp }

                analysisHistory = historyList
                loadingState = false // Ocultar loading cuando se cargan los datos
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error obtaining the history", it)
                loadingState = false // Ocultar loading en caso de error
            }
    }

    // Contenedor principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(listOf(Color.Cyan, Color.Blue)))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loadingState -> {
                // Indicador de carga mientras se obtienen los datos
                CircularProgressIndicator(color = Color.White)
            }

            analysisHistory.isEmpty() -> {
                // Mensaje si no hay historial
                Text(
                    text = "No analysis history found.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            else -> {
                // Mostrar la lista de análisis previos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(analysisHistory) { analysisItem ->
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(analysisItem.timestamp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate(Screen.HistoryResult.createRoute(analysisItem.docId, "","1900-01-01", SimpleDateFormat("yyyy-MM-dd").format(Date()), 1, 5, "", "Select emotion or sentiment"))
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Analysis ID: ${analysisItem.docId}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "URL Analyzed: ",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Date: $formattedDate",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryResults(
    navController: NavController,
    auth: FirebaseAuth,
    docId: String,
    word: String,
    fromDate: Date,
    toDate: Date,
    minStars: Int,
    maxStars: Int,
    commonWordsFiltered: String?,
    emotionOrSentiment: String)  {


    var analysisResults by remember { mutableStateOf<List<ReviewData>>(emptyList()) }
    var commonWords by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loadingState by remember { mutableStateOf(true) }
    var FiltredResults by remember { mutableStateOf<List<ReviewData>>(emptyList()) }

    val context = LocalContext.current
    val userID = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(docId) {
        db.collection("users").document(userID)
            .collection("analysis").document(docId).get()
            .addOnSuccessListener { result ->
                commonWords = (result.get("common_words") as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
                analysisResults = (result.get("analysis_results") as? List<Map<String, Any>>)
                    ?.mapNotNull { review ->
                        val text = review["text"] as? String ?: return@mapNotNull null
                        val sentiment = review["sentiment"] as? String ?: return@mapNotNull null
                        val emotion = review["emotion"] as? String ?: return@mapNotNull null
                        ReviewData(text, sentiment, emotion)
                    } ?: emptyList()

                FiltredResults = analysisResults

                if (word != ""){
                    FiltredResults = FiltredResults.filter { it.text.contains(word, ignoreCase = true) }
                }

                if (emotionOrSentiment != "Select emotion or sentiment"){
                    val filterLower = emotionOrSentiment.lowercase()
                    if (emotionOrSentiment.contains("sentiment")){
                        FiltredResults = FiltredResults.filter { it.sentiment.lowercase() == filterLower.replace(" (sentiment)", "") }
                    }else if (emotionOrSentiment.contains("emotion")){
                        FiltredResults = FiltredResults.filter { it.emotion.lowercase() == filterLower.replace(" (emotion)", "")}
                    }
                }
                loadingState = false
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error fetching analysis", it)
                loadingState = false
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color.Cyan, Color.Blue)))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (loadingState) {
            item {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            item {
                Button(
                    onClick = { navController.navigate(Screen.Filters.createRoute(docId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        text = "Filters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            val positive = FiltredResults.count { it.sentiment == "POSITIVE" }
            val negative = FiltredResults.count { it.sentiment == "NEGATIVE" }
            val neutral = FiltredResults.count { it.sentiment == "NEUTRAL" }
            val total = FiltredResults.size
            val emotionCounts = mutableMapOf<String, Int>().apply {
                val emotionsList = listOf(
                    "neutral", "joy", "sadness", "fear", "disgust", "surprise",
                    "anger", "optimism", "love", "pride", "shame", "guilt", "relief",
                    "gratitude", "trust", "boredom", "amusement", "contentment",
                    "admiration", "annoyance", "determination", "despair",
                    "embarrassment", "enthusiasm", "excitement", "happiness",
                    "hope", "curiosity"
                )
                emotionsList.forEach { put(it, 0) }
            }
            FiltredResults.forEach { review ->
                emotionCounts[review.emotion] = emotionCounts.getOrDefault(review.emotion, 0) + 1
            }


            item {
                if (commonWordsFiltered == "") {
                    DrawCard(positive, negative, neutral, total, commonWords, word, emotionCounts)
                }else{
                    val commonWordsToUse: Map<String, Int> = Gson().fromJson(commonWordsFiltered, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
                    DrawCard(positive, negative, neutral, total, commonWordsToUse, word, emotionCounts)
                }
            }
        }
    }
}