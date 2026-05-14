package com.namma.hasiru

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.namma.hasiru.data.NammaHasiruDatabase
import com.namma.hasiru.data.PlantType
import com.namma.hasiru.data.Plantation
import com.namma.hasiru.data.PlantationRepository
import com.namma.hasiru.data.PlantationStatus
import com.namma.hasiru.data.Species
import com.namma.hasiru.data.StatusUpdate
import com.namma.hasiru.util.compressImage
import com.namma.hasiru.util.createCameraUri
import com.namma.hasiru.util.daysAgo
import com.namma.hasiru.util.formatDate
import com.namma.hasiru.util.resolveAddress
import com.namma.hasiru.util.scoreForStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val PROFILE_PREFS = "namma_hasiru_profile"
private const val KEY_PROFILE_NAME = "profile_name"
private const val KEY_PROFILE_PLACE = "profile_place"
private const val KEY_PROFILE_PHOTO = "profile_photo"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NammaHasiruTheme { HasiruApp(intent.getLongExtra("plantationId", -1L)) } }
    }
}

@Composable
fun NammaHasiruTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = Color(0xFF2E7D32),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDDEFD5),
        secondary = Color(0xFF6D8F3F),
        background = Color(0xFFF5F7F2),
        surface = Color.White,
        error = Color(0xFFD32F2F)
    )
    MaterialTheme(colorScheme = scheme, content = { Surface(color = scheme.background, content = content) })
}

@Composable
fun HasiruApp(deepLinkPlantationId: Long) {
    val nav = rememberNavController()
    LaunchedEffect(deepLinkPlantationId) {
        if (deepLinkPlantationId > 0) nav.navigate("plantation_detail/$deepLinkPlantationId")
    }
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") { SplashScreen(nav) }
        composable("onboarding") { OnboardingScreen(nav) }
        composable("home") { HomeScreen(nav, hasiruViewModel()) }
        composable("add_plantation") { AddPlantationScreen(nav, hasiruViewModel()) }
        composable("map?lat={lat}&lng={lng}", arguments = listOf(
            navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null }
        )) {
            MapScreen(nav, hasiruViewModel(), it.arguments?.getString("lat")?.toDoubleOrNull(), it.arguments?.getString("lng")?.toDoubleOrNull())
        }
        composable("species") { SpeciesScreen(nav, hasiruViewModel()) }
        composable("settings") { SettingsScreen(nav, hasiruViewModel()) }
        composable("plantation_detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            PlantationDetailScreen(nav, it.arguments?.getLong("id") ?: 0L, hasiruViewModel())
        }
    }
}

@Composable
inline fun <reified T : ViewModel> hasiruViewModel(): T {
    val context = LocalContext.current.applicationContext as Application
    return viewModel(factory = HasiruViewModelFactory(context))
}

class HasiruViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = PlantationRepository(NammaHasiruDatabase.getDatabase(app))
        return when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(app, repo)
            AddPlantationViewModel::class.java -> AddPlantationViewModel(app, repo)
            DetailViewModel::class.java -> DetailViewModel(app, repo)
            MapViewModel::class.java -> MapViewModel(repo)
            SpeciesViewModel::class.java -> SpeciesViewModel(repo)
            SettingsViewModel::class.java -> SettingsViewModel(app)
            else -> error("Unknown ViewModel ${modelClass.name}")
        } as T
    }
}

class HomeViewModel(app: Application, repo: PlantationRepository) : AndroidViewModel(app) {
    val plantations = repo.plantations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val stats = repo.userStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.namma.hasiru.data.UserStats())
}

class AddPlantationViewModel(private val app: Application, private val repo: PlantationRepository) : AndroidViewModel(app) {
    val step = MutableStateFlow(1)
    val photoUri = MutableStateFlow<String?>(null)
    val location = MutableStateFlow<Location?>(null)
    val address = MutableStateFlow("Capturing location...")
    val plantType = MutableStateFlow(PlantType.SAPLING)
    val selectedSpecies = MutableStateFlow<Species?>(null)
    val soilType = MutableStateFlow<String?>(null)
    val notes = MutableStateFlow("")
    val saving = MutableStateFlow(false)
    val species = repo.species.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun captureLocation() {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                runCatching {
                    LocationServices.getFusedLocationProviderClient(app)
                        .getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                        .await()
                }.getOrNull()?.let {
                    location.value = it
                    address.value = resolveAddress(app, it.latitude, it.longitude)
                } ?: run { address.value = "Location unavailable. Saved with Bengaluru fallback."; fallbackLocation() }
            } else {
                fallbackLocation()
            }
        }
    }

    private fun fallbackLocation() {
        location.value = Location("fallback").apply {
            latitude = 12.9716
            longitude = 77.5946
        }
        address.value = "Bengaluru fallback location"
    }

    fun setPhoto(uri: Uri) = viewModelScope.launch { photoUri.value = compressImage(app, uri) }
    fun removePhoto() { photoUri.value = null }
    fun next() { if (step.value < 3) step.value += 1 }
    fun back() { if (step.value > 1) step.value -= 1 }

    fun save(onDone: () -> Unit) {
        val speciesValue = selectedSpecies.value ?: return
        val loc = location.value ?: return
        val photo = photoUri.value ?: return
        viewModelScope.launch {
            saving.value = true
            repo.addPlantation(
                Plantation(
                    speciesName = speciesValue.speciesName,
                    commonName = speciesValue.commonName,
                    plantedDate = System.currentTimeMillis(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    locationAddress = address.value,
                    initialPhotoUri = photo,
                    plantType = plantType.value,
                    currentStatus = PlantationStatus.PLANTED,
                    survivalScore = 25,
                    soilType = soilType.value,
                    notes = notes.value.ifBlank { null },
                    nextReminderDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90)
                )
            )
            saving.value = false
            onDone()
        }
    }
}

class DetailViewModel(private val app: Application, private val repo: PlantationRepository) : AndroidViewModel(app) {
    private val id = MutableStateFlow(0L)
    val plantation = id.map { if (it == 0L) null else repo.plantation(it).first() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val updates = id.map { if (it == 0L) emptyList() else repo.updates(it).first() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(plantationId: Long) { id.value = plantationId }
    fun delete(onDone: () -> Unit) = viewModelScope.launch { plantation.value?.let { repo.deletePlantation(it) }; onDone() }
    fun addUpdate(status: PlantationStatus, image: Uri?, notes: String, height: String) = viewModelScope.launch {
        val plant = plantation.value ?: return@launch
        val compressed = image?.let { compressImage(app, it) }
        repo.addStatusUpdate(
            plant,
            StatusUpdate(
                plantationId = plant.id,
                updateDate = System.currentTimeMillis(),
                status = status,
                photoUri = compressed,
                heightCm = height.toFloatOrNull(),
                healthNotes = notes.ifBlank { null },
                survivalScore = scoreForStatus(status.name)
            )
        )
        load(plant.id)
    }
    fun deleteUpdate(update: StatusUpdate) = viewModelScope.launch { repo.deleteStatusUpdate(update); load(id.value) }
}

class MapViewModel(repo: PlantationRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val status = MutableStateFlow<PlantationStatus?>(null)
    val allPlantations = combine(repo.plantations, query, status) { list, q, s ->
        list.filter { (q.isBlank() || it.commonName.contains(q, true) || it.speciesName.contains(q, true)) && (s == null || it.currentStatus == s) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val selected = MutableStateFlow<Plantation?>(null)
}

class SpeciesViewModel(repo: PlantationRepository) : ViewModel() {
    val species = repo.species.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val topSpecies = repo.topSpecies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class SettingsViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)

    val profileName = MutableStateFlow(prefs.getString(KEY_PROFILE_NAME, "Namma User").orEmpty().ifBlank { "Namma User" })
    val profilePlace = MutableStateFlow(prefs.getString(KEY_PROFILE_PLACE, "Bengaluru, Karnataka").orEmpty().ifBlank { "Bengaluru, Karnataka" })
    val profilePhotoUri = MutableStateFlow(prefs.getString(KEY_PROFILE_PHOTO, null))
    val editing = MutableStateFlow(false)

    fun startEditing() {
        editing.value = true
    }

    fun setProfilePhoto(source: Uri) {
        profilePhotoUri.value = compressImage(app, source)
    }

    fun saveProfile() {
        val name = profileName.value.trim().ifBlank { "Namma User" }
        val place = profilePlace.value.trim().ifBlank { "Bengaluru, Karnataka" }
        profileName.value = name
        profilePlace.value = place
        prefs.edit {
            putString(KEY_PROFILE_NAME, name)
            putString(KEY_PROFILE_PLACE, place)
            putString(KEY_PROFILE_PHOTO, profilePhotoUri.value)
        }
        editing.value = false
    }
}

@Composable
fun SplashScreen(nav: NavHostController) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        kotlinx.coroutines.delay(1200)
        nav.navigate("onboarding") { popUpTo("splash") { inclusive = true } }
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF8BC34A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale.value)) {
            Icon(Icons.Default.Spa, null, Modifier.size(112.dp), tint = Color.White)
            Spacer(Modifier.height(20.dp))
            Text("Namma-Hasiru", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Growing Tomorrow", color = Color.White.copy(alpha = 0.86f))
        }
    }
}

@Composable
fun OnboardingScreen(nav: NavHostController) {
    var page by remember { mutableStateOf(0) }
    val pages = listOf(
        Triple(Icons.Default.Place, "Track Your Trees", "Geo-tag every seed ball and sapling with photos and survival history."),
        Triple(Icons.Default.Notifications, "Never Forget to Check", "Ninety-day reminders help turn plantation drives into care cycles."),
        Triple(Icons.Default.Forest, "Build Community Data", "Local survival data shows which species grow best in each region.")
    )
    Scaffold(topBar = {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { nav.navigate("home") { popUpTo("onboarding") { inclusive = true } } }) { Text("Skip") }
            Text("${page + 1}/3")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(52.dp))
            Icon(pages[page].first, null, Modifier.size(132.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(40.dp))
            Text(pages[page].second, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(pages[page].third, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF53634D))
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(3) {
                    Box(Modifier.padding(5.dp).size(if (it == page) 13.dp else 8.dp).background(if (it == page) MaterialTheme.colorScheme.primary else Color(0xFFC8D7C1), CircleShape))
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (page < 2) page++ else nav.navigate("home") { popUpTo("onboarding") { inclusive = true } } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (page < 2) "Next" else "Get Started") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController, vm: HomeViewModel) {
    val plantations by vm.plantations.collectAsState()
    val stats by vm.stats.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Namma-Hasiru", fontWeight = FontWeight.Bold) },
                actions = { IconButton({ nav.navigate("settings") }) { Icon(Icons.Default.Person, "Profile") } }
            )
        },
        bottomBar = { HasiruBottomBar(nav) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate("add_plantation") },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Plant") }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(4.dp)); ImpactCard(stats.totalPlanted, stats.survivalRate, stats.remindersDue) }
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction(Icons.Default.CameraAlt, "Add Plant", { nav.navigate("add_plantation") }, Modifier.weight(1f))
                    QuickAction(Icons.Default.Map, "Map View", { nav.navigate("map?lat=&lng=") }, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Plantations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton({ nav.navigate("species") }) { Text("Species Guide") }
                }
            }
            if (plantations.isEmpty()) item { EmptyState("No plantations yet", "Add your first seed ball or sapling to begin tracking survival.") }
            items(plantations.take(8)) { plant -> PlantationListItem(plant, { nav.navigate("plantation_detail/${plant.id}") }) }
            item { Spacer(Modifier.height(86.dp)) }
        }
    }
}

@Composable
fun HasiruBottomBar(nav: NavHostController) {
    val route = nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    NavigationBar {
        listOf(
            Triple("home", Icons.Default.Forest, "Home"),
            Triple("map?lat=&lng=", Icons.Default.Map, "Map"),
            Triple("add_plantation", Icons.Default.Add, "Add"),
            Triple("species", Icons.Default.Spa, "Species"),
            Triple("settings", Icons.Default.Person, "Profile")
        ).forEach { (target, icon, label) ->
            NavigationBarItem(
                selected = route.startsWith(target.substringBefore("?")),
                onClick = { nav.navigate(target) },
                icon = { Icon(icon, label) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun ImpactCard(total: Int, survival: Float, due: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Forest, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("Your Impact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(total.toString(), "Trees")
                StatItem("${survival.toInt()}%", "Survival")
                StatItem(due.toString(), "Due")
            }
        }
    }
}

@Composable fun StatItem(value: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(label, color = Color(0xFF52634F)) } }

@Composable
fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(104.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PlantationListItem(plant: Plantation, onClick: () -> Unit) {
    val needsCheckup = daysAgo(plant.plantedDate) >= 90 && plant.lastCheckedDate == null
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(plant.initialPhotoUri, null, Modifier.size(58.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(plant.commonName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Planted ${daysAgo(plant.plantedDate)} days ago", color = Color(0xFF60705C))
                Text("Status: ${plant.currentStatus.name}", color = statusColor(plant.currentStatus), fontWeight = FontWeight.SemiBold)
            }
            if (needsCheckup) Icon(Icons.Default.Warning, "Check-up pending", tint = Color(0xFFFFA000))
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Spa, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF60705C))
        }
    }
}

fun statusColor(status: PlantationStatus): Color = when (status) {
    PlantationStatus.PLANTED -> Color(0xFFFFA000)
    PlantationStatus.SPROUTED -> Color(0xFF0288D1)
    PlantationStatus.GROWING -> Color(0xFF43A047)
    PlantationStatus.HEALTHY -> Color(0xFF1B5E20)
    PlantationStatus.DIED -> Color(0xFFD32F2F)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantationScreen(nav: NavHostController, vm: AddPlantationViewModel) {
    val context = LocalContext.current
    val step by vm.step.collectAsState()
    val photo by vm.photoUri.collectAsState()
    val location by vm.location.collectAsState()
    val address by vm.address.collectAsState()
    val species by vm.species.collectAsState()
    val selectedSpecies by vm.selectedSpecies.collectAsState()
    val plantType by vm.plantType.collectAsState()
    val soilType by vm.soilType.collectAsState()
    val notes by vm.notes.collectAsState()
    val saving by vm.saving.collectAsState()
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val permissions = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.captureLocation() }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let(vm::setPhoto) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = cameraUri
        if (ok && uri != null) vm.setPhoto(uri)
    }
    LaunchedEffect(Unit) { permissionLauncher.launch(permissions) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Add New Plantation") },
            navigationIcon = { IconButton({ if (step > 1) vm.back() else nav.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Back") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(progress = step / 3f, modifier = Modifier.fillMaxWidth())
            when (step) {
                1 -> PhotoStep(
                    photo = photo,
                    address = address,
                    location = location,
                    onCamera = {
                        val uri = createCameraUri(context)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    onGallery = { galleryLauncher.launch("image/*") },
                    onRemove = vm::removePhoto,
                    onNext = { if (photo != null) vm.next() }
                )
                2 -> DetailsStep(
                    species = species,
                    selected = selectedSpecies,
                    onSelected = { vm.selectedSpecies.value = it },
                    plantType = plantType,
                    onPlantType = { vm.plantType.value = it },
                    soilType = soilType,
                    onSoil = { vm.soilType.value = it },
                    notes = notes,
                    onNotes = { vm.notes.value = it },
                    onNext = { if (selectedSpecies != null) vm.next() }
                )
                3 -> ReviewStep(
                    photo = photo,
                    selected = selectedSpecies,
                    plantType = plantType,
                    soilType = soilType,
                    notes = notes,
                    address = address,
                    location = location,
                    saving = saving,
                    onSave = { vm.save { nav.navigate("home") { popUpTo("add_plantation") { inclusive = true } } } }
                )
            }
        }
    }
}

@Composable
fun PhotoStep(photo: String?, address: String, location: Location?, onCamera: () -> Unit, onGallery: () -> Unit, onRemove: () -> Unit, onNext: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 1 of 3: Add Photo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth().height(320.dp)) {
            if (photo == null) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Image, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Add a plantation photo")
                }
            } else {
                Box {
                    AsyncImage(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).background(Color.White.copy(alpha = .9f), CircleShape)) { Icon(Icons.Default.Delete, "Remove photo") }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onCamera, modifier = Modifier.weight(1f).height(52.dp)) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Camera") }
            OutlinedButton(onClick = onGallery, modifier = Modifier.weight(1f).height(52.dp)) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Gallery") }
        }
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(6.dp)); Text(address) }
                location?.let { Text("Lat ${"%.4f".format(it.latitude)}, Lng ${"%.4f".format(it.longitude)}", color = Color(0xFF52634F)) }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, enabled = photo != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) { Text("Next: Details") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsStep(
    species: List<Species>,
    selected: Species?,
    onSelected: (Species) -> Unit,
    plantType: PlantType,
    onPlantType: (PlantType) -> Unit,
    soilType: String?,
    onSoil: (String?) -> Unit,
    notes: String,
    onNotes: (String) -> Unit,
    onNext: () -> Unit
) {
    val soils = listOf("Loamy", "Clay", "Sandy", "Silt", "Peaty", "Chalky", "Well-drained", "Rich soil")
    var speciesExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Step 2 of 3: Enter Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
        item {
            Text("Plant Type", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(plantType == PlantType.SEED_BALL, { onPlantType(PlantType.SEED_BALL) }, { Text("Seed Ball") }, modifier = Modifier.weight(1f))
                FilterChip(plantType == PlantType.SAPLING, { onPlantType(PlantType.SAPLING) }, { Text("Sapling") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            ExposedDropdownMenuBox(speciesExpanded, { speciesExpanded = !speciesExpanded }) {
                OutlinedTextField(
                    value = selected?.commonName.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Species Name *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(speciesExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(speciesExpanded, { speciesExpanded = false }) {
                    species.forEach { s -> DropdownMenuItem(text = { Column { Text(s.commonName); Text(s.scientificName, style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }, onClick = { onSelected(s); speciesExpanded = false }) }
                }
            }
        }
        item {
            ExposedDropdownMenuBox(soilExpanded, { soilExpanded = !soilExpanded }) {
                OutlinedTextField(
                    value = soilType.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Soil Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(soilExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(soilExpanded, { soilExpanded = false }) {
                    DropdownMenuItem(text = { Text("Clear") }, onClick = { onSoil(null); soilExpanded = false })
                    soils.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSoil(it); soilExpanded = false }) }
                }
            }
        }
        item { OutlinedTextField(notes, onNotes, Modifier.fillMaxWidth().height(128.dp), label = { Text("Notes") }, maxLines = 5) }
        item { Button(onClick = onNext, enabled = selected != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) { Text("Next: Review") } }
    }
}

@Composable
fun ReviewStep(photo: String?, selected: Species?, plantType: PlantType, soilType: String?, notes: String, address: String, location: Location?, saving: Boolean, onSave: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Step 3 of 3: Review & Confirm", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
        item { AsyncImage(photo, null, Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop) }
        item {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Species", selected?.commonName.orEmpty())
                DetailRow("Scientific", selected?.scientificName.orEmpty())
                DetailRow("Type", plantType.name.replace("_", " "))
                DetailRow("Location", address)
                location?.let { DetailRow("Coordinates", "%.4f, %.4f".format(it.latitude, it.longitude)) }
                soilType?.let { DetailRow("Soil", it) }
                if (notes.isNotBlank()) DetailRow("Notes", notes)
            } }
        }
        item { Button(onClick = onSave, enabled = !saving && photo != null && selected != null && location != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) { if (saving) CircularProgressIndicator(Modifier.size(22.dp)) else { Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Plant It!") } } }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF60705C))
        Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantationDetailScreen(nav: NavHostController, id: Long, vm: DetailViewModel) {
    LaunchedEffect(id) { vm.load(id) }
    val plant by vm.plantation.collectAsState()
    val updates by vm.updates.collectAsState()
    var showDelete by remember { mutableStateOf(false) }
    var showUpdate by remember { mutableStateOf(false) }
    if (plant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val p = plant!!
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(p.commonName) },
                navigationIcon = { IconButton({ nav.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton({ showDelete = true }) { Icon(Icons.Default.Delete, "Delete") } }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button({ showUpdate = true }, Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Update Status")
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PhotoGallery(listOf(p.initialPhotoUri) + updates.mapNotNull { it.photoUri }) }
            item { StatusCard(p) }
            item {
                SectionCard("Location") {
                    Text(p.locationAddress)
                    Text("%.4f, %.4f".format(p.latitude, p.longitude), color = Color(0xFF60705C))
                    TextButton({ nav.navigate("map?lat=${p.latitude}&lng=${p.longitude}") }) { Icon(Icons.Default.Map, null); Spacer(Modifier.width(4.dp)); Text("View on Map") }
                }
            }
            item {
                SectionCard("Details") {
                    DetailRow("Type", p.plantType.name.replace("_", " "))
                    DetailRow("Planted", formatDate(p.plantedDate))
                    p.soilType?.let { DetailRow("Soil", it) }
                    DetailRow("Reminder", p.nextReminderDate?.let(::formatDate) ?: "Off")
                }
            }
            if (!p.notes.isNullOrBlank()) item { SectionCard("Notes") { Text(p.notes) } }
            item { Text("Growth Timeline", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (updates.isEmpty()) item { Box(Modifier.padding(horizontal = 16.dp)) { EmptyState("No updates yet", "Add a status update with a fresh photo after your visit.") } }
            items(updates) { update -> TimelineItem(update, onDelete = { vm.deleteUpdate(update) }) }
            item { Spacer(Modifier.height(84.dp)) }
        }
    }
    if (showDelete) ConfirmDeleteDialog("Delete Plantation?", "This removes the plantation and all status updates.") { yes ->
        showDelete = false
        if (yes) vm.delete { nav.navigate("home") { popUpTo("home") { inclusive = true } } }
    }
    if (showUpdate) UpdateStatusDialog(onDismiss = { showUpdate = false }, onSave = { status, image, notes, height ->
        vm.addUpdate(status, image, notes, height)
        showUpdate = false
    })
}

@Composable
fun PhotoGallery(photos: List<String>) {
    var index by remember { mutableStateOf(0) }
    val safePhotos = photos.ifEmpty { listOf("") }
    Card(Modifier.fillMaxWidth().height(290.dp).padding(horizontal = 16.dp)) {
        Box {
            AsyncImage(safePhotos[index.coerceAtMost(safePhotos.lastIndex)], null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (safePhotos.size > 1) Row(Modifier.align(Alignment.BottomCenter).padding(10.dp), horizontalArrangement = Arrangement.Center) {
                safePhotos.indices.forEach { Box(Modifier.padding(3.dp).size(if (it == index) 10.dp else 7.dp).background(if (it == index) Color.White else Color.White.copy(alpha = .5f), CircleShape).clickable { index = it }) }
            }
        }
    }
}

@Composable
fun StatusCard(plant: Plantation) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = statusColor(plant.currentStatus).copy(alpha = .12f))) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(14.dp).background(statusColor(plant.currentStatus), CircleShape))
                Spacer(Modifier.width(10.dp))
                Text("Status: ${plant.currentStatus.name}", color = statusColor(plant.currentStatus), fontWeight = FontWeight.Bold)
            }
            Text("Planted ${daysAgo(plant.plantedDate)} days ago")
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(progress = plant.survivalScore / 100f, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(8.dp)), color = statusColor(plant.currentStatus))
                Spacer(Modifier.width(10.dp))
                Text("${plant.survivalScore}%", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun TimelineItem(update: StatusUpdate, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            update.photoUri?.let { AsyncImage(it, null, Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.width(12.dp)) }
            Column(Modifier.weight(1f)) {
                Text(update.status.name, color = statusColor(update.status), fontWeight = FontWeight.Bold)
                Text(formatDate(update.updateDate), color = Color(0xFF60705C))
                update.heightCm?.let { Text("Height: ${it} cm") }
                update.healthNotes?.let { Text(it) }
            }
            IconButton(onDelete) { Icon(Icons.Default.Delete, "Delete update", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(title: String, text: String, onAnswer: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAnswer(false) },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton({ onAnswer(true) }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton({ onAnswer(false) }) { Text("Cancel") } }
    )
}

@Composable
fun UpdateStatusDialog(onDismiss: () -> Unit, onSave: (PlantationStatus, Uri?, String, String) -> Unit) {
    var status by remember { mutableStateOf(PlantationStatus.GROWING) }
    var image by remember { mutableStateOf<Uri?>(null) }
    var notes by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { image = it }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PlantationStatus.values().toList()) { s ->
                    Row(Modifier.fillMaxWidth().clickable { status = s }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(status == s, { status = s })
                        Text(s.name)
                    }
                }
                item { OutlinedButton({ gallery.launch("image/*") }, Modifier.fillMaxWidth()) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text(if (image == null) "Add Photo from Gallery" else "Photo Added") } }
                item { OutlinedTextField(height, { height = it }, Modifier.fillMaxWidth(), label = { Text("Height in cm") }) }
                item { OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Health notes") }) }
            }
        },
        confirmButton = { TextButton({ onSave(status, image, notes, height) }) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(nav: NavHostController, vm: MapViewModel, lat: Double?, lng: Double?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val plantations by vm.allPlantations.collectAsState()
    val selected by vm.selected.collectAsState()
    val query by vm.query.collectAsState()
    val status by vm.status.collectAsState()
    val centerLat = lat ?: plantations.firstOrNull()?.latitude ?: 12.9716
    val centerLng = lng ?: plantations.firstOrNull()?.longitude ?: 77.5946
    val mapSearchLabel = mapSearchLabel(status)
    Scaffold(topBar = {
        TopAppBar(title = { OutlinedTextField(query, { vm.query.value = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search species") }) })
    }, bottomBar = { HasiruBottomBar(nav) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 16.dp, 16.dp, 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    onClick = {
                        scope.launch { openGoogleMaps(context, mapSearchLabel, centerLat, centerLng) }
                    }
                )
                {
                    Column(
                        Modifier.fillMaxWidth().padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Map, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Open Google Maps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Tap to view ${mapSearchLabel.lowercase()} in Google Maps.",
                            color = Color(0xFF60705C)
                        )
                        Button(onClick = { scope.launch { openGoogleMaps(context, mapSearchLabel, centerLat, centerLng) } }) {
                            Text("Open ${mapSearchLabel}")
                        }
                    }
                }

                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(status == null, { vm.status.value = null }, { Text("All") })
                        PlantationStatus.values().forEach { s -> FilterChip(status == s, { vm.status.value = s }, { Text(s.name.lowercase().replaceFirstChar { it.uppercase() }) }) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Plantations on the map", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tap a plantation to view details or open navigation.", color = Color(0xFF60705C))
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (plantations.isEmpty()) {
                            item { EmptyState("No plantations match this map", "Try a different search or status filter.") }
                        } else {
                            items(plantations) { p -> PlantationListItem(p) { vm.selected.value = p } }
                            item { Spacer(Modifier.height(72.dp)) }
                        }
                    }
                }
            }

            selected?.let { p ->
                ModalBottomSheet(onDismissRequest = { vm.selected.value = null }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(p.initialPhotoUri, null, Modifier.size(86.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.commonName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(p.currentStatus.name, color = statusColor(p.currentStatus))
                            Text("${daysAgo(p.plantedDate)} days old")
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton({ openExternalMap(context, p.latitude, p.longitude) }, Modifier.weight(1f)) { Text("Navigate") }
                        Button({ nav.navigate("plantation_detail/${p.id}") }, Modifier.weight(1f)) { Text("View Details") }
                    }
                }
            }
        }
    }
}

suspend fun openGoogleMaps(context: Context, searchLabel: String, fallbackLat: Double, fallbackLng: Double) {
    if (searchLabel != "Trees near me") {
        val searchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(searchLabel)}")
        val searchIntent = Intent(Intent.ACTION_VIEW, searchUri).apply { setPackage("com.google.android.apps.maps") }
        runCatching {
            val pm = context.packageManager
            if (searchIntent.resolveActivity(pm) != null) {
                context.startActivity(searchIntent)
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, searchUri))
            }
        }
        return
    }

    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    val location = if (hasPermission) {
        runCatching {
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()
        }.getOrNull()
    } else {
        null
    }

    openExternalMap(context, location?.latitude ?: fallbackLat, location?.longitude ?: fallbackLng)
}

fun mapSearchLabel(status: PlantationStatus?): String = when (status) {
    PlantationStatus.PLANTED -> "Planted near me"
    PlantationStatus.SPROUTED -> "Sprouted near me"
    PlantationStatus.GROWING, PlantationStatus.HEALTHY -> "Growing near me"
    PlantationStatus.DIED -> "Died near me"
    null -> "Trees near me"
}

fun openExternalMap(context: Context, lat: Double, lng: Double) {
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng")).apply { setPackage("com.google.android.apps.maps") }
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
    runCatching {
        val pm = context.packageManager
        when {
            mapsIntent.resolveActivity(pm) != null -> context.startActivity(mapsIntent)
            browserIntent.resolveActivity(pm) != null -> context.startActivity(browserIntent)
        }
    }
}

fun markerHue(status: PlantationStatus): Float = when (status) {
    PlantationStatus.PLANTED -> BitmapDescriptorFactory.HUE_YELLOW
    PlantationStatus.SPROUTED -> BitmapDescriptorFactory.HUE_CYAN
    PlantationStatus.GROWING, PlantationStatus.HEALTHY -> BitmapDescriptorFactory.HUE_GREEN
    PlantationStatus.DIED -> BitmapDescriptorFactory.HUE_RED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesScreen(nav: NavHostController, vm: SpeciesViewModel) {
    val species by vm.species.collectAsState()
    val top by vm.topSpecies.collectAsState()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Species?>(null) }
    val filtered = species.filter { query.isBlank() || it.commonName.contains(query, true) || it.scientificName.contains(query, true) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Species Guide") },
                navigationIcon = { IconButton({ nav.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        bottomBar = { HasiruBottomBar(nav) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search species") }, singleLine = true) }
            item {
                Text("Best Trees for Your Region", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Based on local survival records", color = Color(0xFF60705C))
            }
            items(top) { s -> SpeciesCard(s, true) { selected = s } }
            item { Text("All Species (A-Z)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(filtered) { s -> SpeciesCard(s, false) { selected = s } }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    selected?.let { SpeciesDetailSheet(it) { selected = null } }
}

@Composable
fun SpeciesCard(species: Species, featured: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(if (featured) 3.dp else 1.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Forest, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(species.commonName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(species.scientificName, color = Color(0xFF60705C))
                Text("${species.averageSurvivalRate.toInt()}% survival · ${species.totalPlanted} planted locally", color = statusColor(PlantationStatus.GROWING))
                LinearProgressIndicator(species.averageSurvivalRate / 100f, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(8.dp)), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesDetailSheet(species: Species, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Forest, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(species.commonName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(species.scientificName, color = Color(0xFF60705C))
                }
            }
            Text(species.description.orEmpty())
            Divider()
            DetailRow("Category", species.category)
            DetailRow("Ideal soil", species.idealSoilType)
            DetailRow("Water", species.waterRequirement)
            DetailRow("Sunlight", species.sunlightRequirement)
            DetailRow("Local survival", "${species.averageSurvivalRate.toInt()}%")
            DetailRow("Planted locally", species.totalPlanted.toString())
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController, vm: SettingsViewModel) {
    val name by vm.profileName.collectAsState()
    val place by vm.profilePlace.collectAsState()
    val photo by vm.profilePhotoUri.collectAsState()
    val editing by vm.editing.collectAsState()
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(vm::setProfilePhoto)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { IconButton({ nav.navigateUp() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        bottomBar = { HasiruBottomBar(nav) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 18.dp, 16.dp, 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(132.dp),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                if (photo != null) {
                                    AsyncImage(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    vm.startEditing()
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, "Change photo", tint = Color.White)
                            }
                        }

                        if (editing) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { vm.profileName.value = it },
                                label = { Text("Full name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = place,
                                onValueChange = { vm.profilePlace.value = it },
                                label = { Text("Place") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { vm.startEditing(); galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.PhotoLibrary, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Photo")
                                }
                                Button(onClick = vm::saveProfile, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save")
                                }
                            }
                        } else {
                            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(place, color = Color(0xFF60705C))
                            Button(onClick = vm::startEditing) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Edit profile")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Profile details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Your name, place and profile photo are saved on this device.", color = Color(0xFF60705C))
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Version", fontWeight = FontWeight.SemiBold)
                        Text("1.0.0", color = Color(0xFF60705C))
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(title, Modifier.padding(start = 18.dp, top = 18.dp, bottom = 6.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun SwitchPreference(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFF60705C)) }
        Switch(checked, onChange)
    }
}

@Composable
fun PlainPreference(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFF60705C)) }
    }
}

@Composable
fun ClickablePreference(title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White).clickable(onClick = onClick).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFF60705C)) }
        Icon(Icons.Default.FileUpload, null, tint = MaterialTheme.colorScheme.primary)
    }
}
