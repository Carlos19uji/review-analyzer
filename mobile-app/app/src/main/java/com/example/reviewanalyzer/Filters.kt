package com.example.reviewanalyzer

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.exp


@Composable
fun Filters(onBackClick: () -> Unit, navController: NavController, docId: String, auth: FirebaseAuth) {
    var word by remember { mutableStateOf("") }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var minStars by remember { mutableStateOf(1f) }
    var maxStars by remember { mutableStateOf(5f) }
    var analysisResults by remember { mutableStateOf<List<ReviewData>>(emptyList()) }
    var commonWords by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loadingState by remember { mutableStateOf(true) }
    var FiltredResults by remember { mutableStateOf<List<ReviewData>>(emptyList()) }
    var reviewsMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val context = LocalContext.current
    var commonWordsFiltered by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var navigateToHistory by remember { mutableStateOf(false) }
    val expandedEmotionMenu = remember { mutableStateOf(false) }
    val emotionState = remember { mutableStateOf("Select emotion or sentiment") }

    val defaultFromDate = Calendar.getInstance().apply {
        set(Calendar.YEAR, 1900)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
    }.time

    val defaultToDate = Calendar.getInstance().time

    var fromDate by remember { mutableStateOf<Date>(defaultFromDate) }
    var toDate by remember { mutableStateOf<Date?>(defaultToDate) }
    val userID = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val sentimentOptions = listOf("Positive (sentiment)", "Negative (sentiment)", "Neutral (sentiment)")
    val emotionOptions = listOf(
        "Admiration (emotion)", "Amusement (emotion)", "Anger (emotion)", "Annoyance (emotion)",
        "Approval (emotion)", "Caring (emotion)", "Confusion (emotion)", "Curiosity (emotion)",
        "Desire (emotion)", "Disappointment (emotion)", "Disapproval (emotion)", "Disgust (emotion)",
        "Embarrassment (emotion)", "Excitement (emotion)", "Fear (emotion)", "Gratitude (emotion)",
        "Grief (emotion)", "Joy (emotion)", "Love (emotion)", "Nervousness (emotion)", "Optimism (emotion)",
        "Pride (emotion)", "Realization (emotion)", "Relief (emotion)", "Remorse (emotion)",
        "Sadness (emotion)", "Surprise (emotion)", "Neutral (emotion)"
    )
    val allOptions = sentimentOptions + emotionOptions


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(listOf(Color.Cyan, Color.Blue)))
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            InputField("Introduce a key word", value = word, onValueChange = { word = it })
            Spacer(modifier = Modifier.height(16.dp))
        }
        item{
            Column(modifier= Modifier.padding(16.dp)){
                Text(
                    text= "Filter by sentiment or emotion",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { expandedEmotionMenu.value = true }
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ){
                    Row{
                        Text(text = emotionState.value, color=Color.Black)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Black
                        )
                    }
                }
                DropdownMenu(
                    expanded = expandedEmotionMenu.value,
                    onDismissRequest = { expandedEmotionMenu.value = false}
                ) {
                    allOptions.forEach{ option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                emotionState.value = option
                                expandedEmotionMenu.value = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { showFromDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(text = "From: ${formatDate(fromDate)}")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = { showToDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(text = "Until: ${formatDate(toDate)}")
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        item {
            Text("Filter by stars", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Minimum: ${minStars.toInt()} stars", color = Color.White)
                Slider(value = minStars, onValueChange = { minStars = it }, valueRange = 1f..5f, steps = 4)
                Text("Maximum: ${maxStars.toInt()} stars", color = Color.White)
                Slider(value = maxStars, onValueChange = { maxStars = it }, valueRange = 1f..5f, steps = 4)
            }
            Spacer(modifier = Modifier.height(25.dp))
        }

        item {
            Button(
                onClick = {
                    val encodedWord = word
                    val encodedFromDate = formatDate(fromDate)
                    val encodedToDate = formatDate(toDate ?: defaultToDate)

                    if (encodedWord.isNotEmpty() || emotionState.value != "Select emotion or sentiment") {
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

                                if (encodedWord.isNotEmpty()) {
                                    FiltredResults = if (word.isNotEmpty()) {
                                        FiltredResults.filter {
                                            it.text.contains(
                                                word,
                                                ignoreCase = true
                                            )
                                        }
                                    } else {
                                        analysisResults
                                    }
                                }
                                if (emotionState.value != "Select emotion or sentiment"){
                                    val filterLower = emotionState.value.lowercase()
                                    if (emotionState.value.contains("sentiment")){
                                        FiltredResults = analysisResults.filter { it.sentiment.lowercase() == filterLower.replace(" (sentiment)", "") }
                                    }else if (emotionState.value.contains("emotion")){
                                        FiltredResults = analysisResults.filter { it.emotion.lowercase() == filterLower.replace(" (emotion)", "")}
                                    }
                                }

                                reviewsMap = FiltredResults.mapIndexed { index, review -> index to review.text }.toMap()
                                loadingState = false

                                if (encodedWord.isNotEmpty()) {
                                    getRelatedWords(context, word, reviewsMap) { response ->
                                        commonWordsFiltered = response?.related_words ?: emptyMap()
                                        navigateToHistory = true
                                    }
                                }else{
                                    getCommonWords(reviewsMap){ response ->
                                        commonWordsFiltered = response?.related_words ?: emptyMap()
                                        navigateToHistory = true
                                    }
                                }
                            }
                            .addOnFailureListener {
                                Log.e("Firestore", "Error fetching analysis", it)
                                loadingState = false
                            }
                    } else {
                        navController.navigate(
                            Screen.HistoryResult.createRoute(
                                docId,
                                encodedWord,
                                encodedFromDate,
                                encodedToDate,
                                minStars.toInt(),
                                maxStars.toInt(),
                                "",
                                emotionState.value
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "Apply Filters", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    LaunchedEffect(navigateToHistory) {
        if (navigateToHistory) {
            val jsonResponse = Uri.encode(Gson().toJson(commonWordsFiltered))
            navController.navigate(
                Screen.HistoryResult.createRoute(
                    docId,
                    word,
                    formatDate(fromDate),
                    formatDate(toDate ?: defaultToDate),
                    minStars.toInt(),
                    maxStars.toInt(),
                    jsonResponse,
                    emotionState.value
                )
            )
            navigateToHistory = false
        }
    }

    if (showFromDatePicker) {
        DatePickerDialogComponent(
            onDismissRequest = { showFromDatePicker = false },
            onDateSelected = { selectedDate -> fromDate = Date(selectedDate.time) }
        )
    }
    if (showToDatePicker) {
        DatePickerDialogComponent(
            onDismissRequest = { showToDatePicker = false },
            onDateSelected = { selectedDate -> toDate = Date(selectedDate.time) }
        )
    }
}

fun formatDate(date: Date?): String {
    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return date?.let { format.format(it) } ?: "No selection"
}
