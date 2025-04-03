package com.example.reviewanalyzer

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.reviewanalyzer.ui.theme.ReviewAnalyzerTheme
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()



        Notification.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("MainActivity", "Notification permission granted")
                } else {
                    Log.e("MainActivity", "Notification permission denied")
                }
            }
            Notification.requestNotificationPermission(this, requestPermissionLauncher)
        }


        enableEdgeToEdge()
        setContent {
            ReviewAnalyzerTheme {
                navController = rememberNavController()
                MainApp(auth)
            }
        }
    }
}

sealed class Screen(val route: String){
    object Home : Screen("home_screen")
    object History : Screen("history")
    object FirstScreen: Screen("first_screen")
    object Login: Screen("login_screen")
    object CreateAccount: Screen("create_account")
    object Password: Screen("password_recovery")
    object Results : Screen("results_screen/{apiResponse}/{docId}") {
        fun createRoute(response: String, docId: String): String {
            return "results_screen/${Uri.encode(response)}/$docId" // ✅ Codificar response correctamente
        }
    }

    object HistoryResult: Screen("history_result/{docId}/{word}/{fromDate}/{toDate}/{minStars}/{maxStars}/{commonWordsFiltered}/{emotionOrSentiment}") {
        fun createRoute(
            docId: String,
            word: String,
            fromDate: String,
            toDate: String,
            minStars: Int,
            maxStars: Int,
            commonWordsFiltered: String,
            emotionOrSentiment: String
        ): String {
            return "history_result/${docId}/$word/$fromDate/$toDate/$minStars/$maxStars/$commonWordsFiltered/$emotionOrSentiment"
        }
    }

    object Filters: Screen("filters/{docId}"){
        fun createRoute(docId: String): String{
            return "filters/$docId"
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReviewAnalyzerTheme {
        Greeting("Android")
    }
}