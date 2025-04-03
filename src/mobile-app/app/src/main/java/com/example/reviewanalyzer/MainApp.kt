package com.example.reviewanalyzer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun MainApp(auth: FirebaseAuth){
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
            if ( currentScreen == Screen.Filters.route ||
                currentScreen?.startsWith("results_screen") == true ||
                currentScreen?.startsWith("history_result") == true)
            {
                val title = when (currentScreen){
                    Screen.Filters.route -> "Filters"
                    else -> "Analysis Results"
                }
                TopNavigationBar(onBackClick = {navController.popBackStack()}, title = title)
            }
        },

        bottomBar = {
            val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentScreen in listOf(
                    Screen.Home.route,
                    Screen.History.route,
                ) || currentScreen?.startsWith("results_screen") == true ||
                currentScreen?.startsWith("history_result") == true)
            {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FirstScreen.route,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(Screen.Home.route){
                HomeScreen(navController, auth)
            }
            composable(Screen.History.route){
                History(navController, auth)
            }
            composable(Screen.FirstScreen.route){
                FirstScreen(
                    onLoginClick = {navController.navigate(Screen.Login.route)},
                    onCreateAccountClick = {navController.navigate(Screen.CreateAccount.route)})
            }
            composable(Screen.Login.route){
                LoginScreen(
                    auth,
                    navController,
                    onCreateAccountClick = {navController.navigate(Screen.CreateAccount.route)},
                    onForgotPasswordClick = {navController.navigate(Screen.Password.route)}
                )
            }
            composable(Screen.CreateAccount.route) {
                CreateAccount(
                    onLoginClick = {navController.navigate(Screen.Login.route)},
                    navController,
                    auth
                )
            }
            composable(Screen.Password.route) {
                PasswordRecovery(auth, navController)
            }
            composable(
                route = "history_result/{docId}/{word}/{fromDate}/{toDate}/{minStars}/{maxStars}/{commonWordsFiltered}/{emotionOrSentiment}",
                arguments = listOf(
                    navArgument("docId") { type = NavType.StringType },
                    navArgument("word") { type = NavType.StringType },
                    navArgument("fromDate") { type = NavType.StringType },
                    navArgument("toDate") { type = NavType.StringType },
                    navArgument("minStars") { type = NavType.IntType },
                    navArgument("maxStars") { type = NavType.IntType },
                    navArgument("commonWordsFiltered") { type = NavType.StringType},
                    navArgument("emotionOrSentiment"){ type= NavType.StringType}
                )
            ) { backStackEntry ->
                val docId = backStackEntry.arguments?.getString("docId")!!
                val word = backStackEntry.arguments?.getString("word") ?: ""
                val fromDate = SimpleDateFormat("yyyy-MM-dd").parse(backStackEntry.arguments?.getString("fromDate")!!)
                val toDate = SimpleDateFormat("yyyy-MM-dd").parse(backStackEntry.arguments?.getString("toDate")!!)
                val minStars = backStackEntry.arguments?.getInt("minStars")!!
                val maxStars = backStackEntry.arguments?.getInt("maxStars")!!
                val commonWordsFiltered = backStackEntry.arguments?.getString("commonWordsFiltered")
                val emotionOrSentiment = backStackEntry.arguments?.getString("emotionOrSentiment") ?: "Select emotion or sentiment"

                HistoryResults(navController, auth, docId, word, fromDate, toDate, minStars, maxStars, commonWordsFiltered, emotionOrSentiment)
            }
            composable(
                route = "results_screen/{apiResponse}/{docId}",
                arguments = listOf(
                    navArgument("apiResponse") { type = NavType.StringType }, // ✅ Nombre correcto
                    navArgument("docId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val apiResponseJson = backStackEntry.arguments?.getString("apiResponse") // ✅ Nombre correcto
                val docId = backStackEntry.arguments?.getString("docId")!!

                if (apiResponseJson != null) {
                    ResultsScreen(apiResponseJson, navController, docId)
                }
            }
            composable(
                route= Screen.Filters.route,
                arguments = listOf(navArgument("docId"){ type= NavType.StringType })
            ) { backStackEntry ->

                val docId = backStackEntry.arguments?.getString("docId")!!

                Filters(onBackClick = {navController.popBackStack()}, navController, docId, auth)
            }
        }

    }
}