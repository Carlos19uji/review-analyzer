package com.example.reviewanalyzer

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



@Composable
fun HomeScreen(navController: NavController, auth: FirebaseAuth) {
    var word by remember { mutableStateOf("") }
    var analysisResults by remember { mutableStateOf<List<ReviewData>?>(null) }
    var commonWords by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loadingState by remember { mutableStateOf(false) }
    var apiResponse by remember { mutableStateOf<ApiResponse?>(null) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return
    var timestamp = System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(listOf(Color.Cyan, Color.Blue)))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "ReviewAnalyzer",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
            InputField(
                label = "Introduce the URL to analyze the reviews",
                value = word,
                onValueChange = { word = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    loadingState = true
                    fetchAnalysisResults(context) { response ->
                        loadingState = false
                        apiResponse = response
                        analysisResults = response?.analysis_results?.values?.toList() ?: emptyList()
                        commonWords = response?.common_words ?: emptyMap()
                        if (response != null) {
                            timestamp = System.currentTimeMillis()
                            val analysisData = hashMapOf(
                                "timestamp" to timestamp,
                                "word" to word,
                                "analysis_results" to analysisResults,
                                "common_words" to commonWords
                            )

                            db.collection("users").document(userId)
                                .collection("analysis").document(timestamp.toString())
                                .set(analysisData)
                                .addOnSuccessListener {
                                    Log.d("Firebase", "Datos guardados correctamente")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firebase", "Error al guardar en Firestore", e)
                                }
                        }
                    }
                },
                modifier = Modifier
                    .width(180.dp)
                    .height(55.dp)
                    .shadow(10.dp, shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "Analyze", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (loadingState) {
            item {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        analysisResults?.let { reviews ->
            if (reviews.isNotEmpty()) {
                item {
                    apiResponse?.let { response ->
                        val jsonResponse = Uri.encode(Gson().toJson(response))
                        Button(
                            onClick = {
                                navController.navigate(Screen.Results.createRoute(jsonResponse, timestamp.toString()))
                            },
                            modifier = Modifier
                                .width(180.dp)
                                .height(55.dp)
                                .shadow(10.dp, shape = RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(text = "See analysis results", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "No reviews found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.LightGray
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )
    }
}

@Composable
fun PieChart(sortedReviews: List<Triple<String, Int, Color>>, total: Int) {
    val startAngles = mutableListOf<Float>()
    val sweepAngles = mutableListOf<Float>()
    var startAngle = 0f

    sortedReviews.forEach { (_, count, _) ->
        val sweepAngle = if (total > 0) (count.toFloat() / total) * 360 else 0f
        startAngles.add(startAngle)
        sweepAngles.add(sweepAngle)
        startAngle += sweepAngle
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val canvasSize = size
        val centerX = canvasSize.width / 2
        val centerY = canvasSize.height / 2

        sortedReviews.forEachIndexed { index, (_, _, color) ->
            drawArc(
                color = color,
                startAngle = startAngles[index],
                sweepAngle = sweepAngles[index],
                useCenter = true,
                size = canvasSize
            )
        }
    }
}


@Composable
fun ResultsScreen(apiResponseJson: String, navController: NavController, docId: String) {
    val apiResponse = remember {
        Gson().fromJson(Uri.decode(apiResponseJson), ApiResponse::class.java) // ✅ Decodificar antes de parsear
    }

    var word by remember { mutableStateOf("") }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var minStars by remember { mutableStateOf(1f) }
    var maxStars by remember { mutableStateOf(5f) }
    var fromDate by remember { mutableStateOf<Long?>(null) }
    var toDate by remember { mutableStateOf<Long?>(null) }
    val analysisResults = apiResponse.analysis_results?.values?.toList() ?: emptyList()
    val commonWords = apiResponse.common_words ?: emptyMap()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(listOf(Color.Cyan, Color.Blue)))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        analysisResults.let { reviews ->
            if (reviews.isNotEmpty()) {
                val positive = reviews.count { it.sentiment == "POSITIVE" }
                val negative = reviews.count { it.sentiment == "NEGATIVE" }
                val neutral = reviews.count { it.sentiment == "NEUTRAL" }
                val total = reviews.size
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
                reviews.forEach { review ->
                    emotionCounts[review.emotion] = emotionCounts.getOrDefault(review.emotion, 0) + 1
                }


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

                item {
                    DrawCard(positive, negative, neutral, total, commonWords, "", emotionCounts)
                }
                
            } else {
                item {
                    Text(
                        text = "No reviews found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun DatePickerDialogComponent(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    DisposableEffect(Unit) {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
                onDismissRequest()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()

        onDispose { datePicker.dismiss() }
    }
}



@Composable
fun DrawCard(
    positive: Int,
    negative: Int,
    neutral: Int,
    total: Int,
    commonWords: Map<String, Int>,
    word: String,
    emotionsCounts: Map<String, Int>
) {

    val sortedReviews = listOf(
        Triple("Positive", positive, Color(0xFF4CAF50)),
        Triple("Negative", negative, Color(0xFFD32F2F)),
        Triple("Neutral", neutral, Color(0xFF9E9E9E))
    ).sortedByDescending { it.second }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(10.dp, shape = RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Analysis results:",
                fontSize = 25.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(30.dp))
            PieChart(sortedReviews, total)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Total number of reviews: $total",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            sortedReviews.forEach { (type, count, color) ->
                val percentage = if (total > 0) (count * 100f / total) else 0f // Cambio: Usar Float en lugar de Int
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$type: $count (${String.format("%.1f", percentage)}%)",
                        fontSize = 18.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Associated Emotions:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            emotionsCounts.toList()
                .sortedByDescending { it.second }
                .forEach { (emotion, frequency) ->
                    if (frequency > 0) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$emotion: $frequency",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val percentage =
                            if (total > 0) (frequency * 100f / total) else 0f // Cambio: Usar Float en lugar de Int

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = percentage / 100f)
                                    .height(12.dp)
                                    .background(Color(0xFF9C27B0))
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(25.dp))

            if (word.isEmpty()) {
                Text(
                    text = "Top 10 Most Frequent Words in the Reviews:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Top 10 Most Frequent Words in Reviews Which Contains the Word ($word):",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            commonWords.toList()
                .sortedByDescending { it.second }
                .take(10)
                .forEach { (word, frequency) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$word: $frequency",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
        }
    }
}