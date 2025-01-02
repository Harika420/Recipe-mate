package com.example.recipemate

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeMateApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeMateApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RecipeMate") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index -> selectedTab = index }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> CategoriesScreen()
                2 -> SavedRecipesScreen()
                3 -> ShoppingListScreen()
                4 -> ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
            label = { Text("Categories") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved Recipes") },
            label = { Text("Saved") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping List") },
            label = { Text("Shopping") }
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun CategoriesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Categories Screen", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SavedRecipesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Saved Recipes Screen", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ShoppingListScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Shopping List Screen", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun HomeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val recipes = remember { mutableStateListOf<Map<String, String>>() }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun fetchFromSpoonacular(query: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://api.spoonacular.com/recipes/complexSearch?query=$query&apiKey=c5039dea51bc4193a92074c8f607bd2b"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val results = jsonResponse.getJSONArray("results")

                    val fetchedRecipes = (0 until results.length()).map { i ->
                        val recipe = results.getJSONObject(i)
                        mapOf(
                            "title" to recipe.optString("title"),
                            "image" to recipe.optString("image")
                        )
                    }

                    // Save to Firebase Firestore
                    fetchedRecipes.forEach { recipe ->
                        firestore.collection("recipes").add(recipe).await()
                    }

                    recipes.clear()
                    recipes.addAll(fetchedRecipes)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching recipes: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadFromFirebase() {
        coroutineScope.launch {
            try {
                val snapshot = firestore.collection("recipes").get().await()
                val fetchedRecipes = snapshot.documents.map { doc ->
                    doc.data as Map<String, String>
                }
                recipes.clear()
                recipes.addAll(fetchedRecipes)
            } catch (e: Exception) {
                Log.e("FIREBASE_ERROR", "Error fetching recipes from Firestore: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Recipes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (searchQuery.isNotEmpty()) {
                fetchFromSpoonacular(searchQuery)
            } else {
                loadFromFirebase()
            }
        }) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Map<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = recipe["image"],
                contentDescription = recipe["title"],
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(recipe["title"] ?: "No Title", style = MaterialTheme.typography.titleMedium)
        }
    }
}
