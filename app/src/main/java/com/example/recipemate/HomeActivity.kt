package com.example.recipemate

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    var selectedRecipe by remember { mutableStateOf<Map<String, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RecipeMate") }
            )
        },
        bottomBar = {
            if (selectedRecipe == null) { // Hide bottom bar on details screen
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index -> selectedTab = index }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Foreground content
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedRecipe != null) {
                    RecipeDetailsScreen(recipe = selectedRecipe) {
                        selectedRecipe = null // Navigate back to main screen
                    }
                } else {
                    when (selectedTab) {
                        0 -> HomeScreen { recipe ->
                            selectedRecipe = recipe // Navigate to details screen
                        }
                        1 -> CategoriesScreen()
                        2 -> SavedRecipesScreen { recipe ->
                            selectedRecipe = recipe // Navigate to details screen
                        }
                        3 -> ShoppingListScreen()
                        4 -> ProfileScreen()
                    }
                }
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
fun SavedRecipesScreen(onRecipeClick: (Map<String, String>) -> Unit) {
    val savedRecipes = remember { mutableStateListOf<Map<String, String>>() }
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // Load saved recipes from Firestore
    fun loadSavedRecipes() {
        coroutineScope.launch {
            isLoading = true
            isError = false
            try {
                val snapshot = firestore.collection("saved_recipes").get().await()
                val fetchedRecipes = snapshot.documents.map { doc ->
                    doc.data as Map<String, String>
                }
                savedRecipes.clear()
                savedRecipes.addAll(fetchedRecipes)
            } catch (e: Exception) {
                Log.e("FIREBASE_ERROR", "Error fetching saved recipes: ${e.message}")
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    // Load recipes when screen is first displayed
    LaunchedEffect(Unit) {
        loadSavedRecipes()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Foreground content
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (isError) {
            Text(
                text = "Failed to load saved recipes.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (savedRecipes.isEmpty()) {
            Text(
                text = "No saved recipes.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedRecipes) { recipe ->
                    RecipeCard(recipe, onClick = { onRecipeClick(it) })
                }
            }
        }
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
fun HomeScreen(onRecipeClick: (Map<String, String>) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val recipes = remember { mutableStateListOf<Map<String, String>>() }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun fetchFromSpoonacular(query: String) {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            isError = false
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
                            "image" to recipe.optString("image"),
                            "id" to recipe.optString("id")
                        )
                    }

                    withContext(Dispatchers.Main) {
                        recipes.clear()
                        recipes.addAll(fetchedRecipes)
                        if (recipes.isEmpty()) isError = true // Set error if no recipes found
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isError = true
                        Toast.makeText(context, "Failed to fetch recipes", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching recipes: ${e.message}")
                withContext(Dispatchers.Main) {
                    isError = true
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadFromFirebase() {
        coroutineScope.launch {
            isLoading = true
            isError = false
            try {
                val snapshot = firestore.collection("recipes").get().await()
                val fetchedRecipes = snapshot.documents.map { doc ->
                    doc.data as Map<String, String>
                }
                recipes.clear()
                recipes.addAll(fetchedRecipes)
                if (recipes.isEmpty()) isError = true
            } catch (e: Exception) {
                Log.e("FIREBASE_ERROR", "Error fetching recipes from Firestore: ${e.message}")
                isError = true
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
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

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (isError) {
            Text(
                text = "No recipes found. Try a different search.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCard(recipe, onClick = { onRecipeClick(it) })
                }
            }
        }
    }
}


@Composable
fun RecipeCard(recipe: Map<String, String>, onClick: (Map<String, String>) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(recipe) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val imageUrl = recipe["image"] ?: ""
            AsyncImage(
                model = imageUrl,
                contentDescription = recipe["title"],
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(recipe["title"] ?: "No Title", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun RecipeDetailsScreen(recipe: Map<String, String>?, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var instructions by remember { mutableStateOf<String?>(null) }
    val firestore = FirebaseFirestore.getInstance()
    var isSaved by remember { mutableStateOf(false) }

    // Fetch recipe details using the ID
    fun fetchRecipeDetails(recipeId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url =
                    "https://api.spoonacular.com/recipes/$recipeId/information?apiKey=c5039dea51bc4193a92074c8f607bd2b"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val fetchedInstructions = jsonResponse.optString("instructions", "No instructions available.")

                    withContext(Dispatchers.Main) {
                        instructions = fetchedInstructions
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch instructions", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching recipe details: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Save recipe to Firestore
    fun saveRecipe() {
        recipe?.let {
            coroutineScope.launch {
                try {
                    firestore.collection("saved_recipes").add(it).await()
                    isSaved = true
                    Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("FIREBASE_ERROR", "Error saving recipe: ${e.message}")
                    Toast.makeText(context, "Failed to save recipe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Fetch instructions if the recipe has an ID
    LaunchedEffect(recipe?.get("id")) {
        recipe?.get("id")?.let { fetchRecipeDetails(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Foreground content
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Back button
            Button(onClick = onBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe title
            Text(
                text = recipe?.get("title") ?: "No Title",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe image
            AsyncImage(
                model = recipe?.get("image"),
                contentDescription = recipe?.get("title"),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe instructions
            Text(
                text = instructions ?: "Fetching instructions...",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Save button at the top right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Button(
                onClick = { saveRecipe() },
                enabled = !isSaved
            ) {
                Text(if (isSaved) "Saved" else "Save")
            }
        }
    }
}





