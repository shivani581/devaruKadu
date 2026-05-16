package com.example.devarakadu

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devarakadu.ui.theme.DevaraKaduTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSMdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            DevaraKaduTheme {
                PermissionHandler {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun PermissionHandler(content: @Composable () -> Unit) {
    var permissionsGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (permissionsGranted) {
        content()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Location permissions are required to use the Map.")
        }
    }
}

@Composable
fun MainScreen(vm: GroveViewModel = viewModel()) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation(backgroundColor = Color(0xFF2E7D32), contentColor = Color.White) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Directory") },
                    selected = false,
                    onClick = { navController.navigate("directory") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Map") },
                    selected = false,
                    onClick = { navController.navigate("map") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Scan") },
                    selected = false,
                    onClick = { navController.navigate("scan") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    label = { Text("Alert") },
                    selected = false,
                    onClick = { navController.navigate("alert") }
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "directory", modifier = Modifier.padding(padding)) {
            composable("directory") { GroveDirectoryScreen(vm) }
            composable("map") { MapScreen(vm) }
            composable("scan") { SpeciesScanScreen(vm) }
            composable("alert") { ConservationAlertScreen(vm) }
        }
    }
}

@Composable
fun GroveDirectoryScreen(vm: GroveViewModel) {
    val groves by vm.groves.collectAsState()
    val currentLocation by vm.currentLocation.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Grove Directory",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF1B5E20)
        )

        // Simulation Button
        Button(
            onClick = { 
                vm.updateLocation(MyLatLng(12.035, 75.968))
                Toast.makeText(context, "Location Simulated: Near Irupu Grove", Toast.LENGTH_SHORT).show()
            }, // Near Irupu
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8BC34A))
        ) {
            Text("Simulate Being Near Irupu Grove", color = Color.White)
        }

        groves.forEach { grove ->
            val isUnlocked = vm.isNear(grove, currentLocation)
            Card(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(grove.name, style = MaterialTheme.typography.h6, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                        if (isUnlocked) {
                            Icon(Icons.Default.CheckCircle, "Unlocked", tint = Color(0xFF4CAF50))
                        } else {
                            Icon(Icons.Default.Lock, "Locked", tint = Color.Gray)
                        }
                    }
                    Text("Deity: ${grove.deity} | Type: ${grove.type}", style = MaterialTheme.typography.caption)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (isUnlocked) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            // Section: Traditional Beliefs
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Belief", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("TRADITIONAL BELIEF", style = MaterialTheme.typography.overline, color = Color(0xFFB26A00))
                            }
                            Text(grove.myth, style = MaterialTheme.typography.body2, modifier = Modifier.padding(bottom = 12.dp))

                            // Section: Scientific Facts
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, "Science", tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SCIENTIFIC FACT", style = MaterialTheme.typography.overline, color = Color(0xFF1565C0))
                            }
                            Text(grove.scientificFact, style = MaterialTheme.typography.body2, color = Color(0xFF2E7D32))
                        }
                    } else {
                        Text("📍 Visit this grove to unlock its sacred history!", style = MaterialTheme.typography.body2, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MapScreen(vm: GroveViewModel) {
    val groves by vm.groves.collectAsState()
    val coorg = GeoPoint(12.3375, 75.8069)

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(12.0)
                controller.setCenter(coorg)
                setMultiTouchControls(true)

                // Add User Location
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            mapView.overlays.removeIf { it is Marker }
            groves.forEach { grove ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(grove.latitude, grove.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = grove.name
                marker.snippet = "Sacred to ${grove.deity}"
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}

@Composable
fun SpeciesScanScreen(vm: GroveViewModel) {
    val scannedSpecies by vm.scannedSpecies.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Species Identifier", style = MaterialTheme.typography.h5, color = Color(0xFF1B5E20))
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Camera Viewfinder\n(Simulated)", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { vm.scanSpecies() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
        ) {
            Text("SCAN SPECIES", color = Color.White)
        }

        scannedSpecies?.let { species ->
            Spacer(modifier = Modifier.height(24.dp))
            Card(elevation = 4.dp, shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(species.name, style = MaterialTheme.typography.h6, color = Color(0xFF2E7D32))
                    Text(species.scientificName, style = MaterialTheme.typography.caption, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(species.description, style = MaterialTheme.typography.body2)
                    Text("Significance: ${species.significance}", style = MaterialTheme.typography.body2, color = Color(0xFF388E3C))
                }
            }
        }
    }
}

@Composable
fun ConservationAlertScreen(vm: GroveViewModel) {
    var alertText by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Report Conservation Issue", style = MaterialTheme.typography.h6)
        OutlinedTextField(
            value = alertText,
            onValueChange = { alertText = it },
            label = { Text("Example: Illegal tree felling or waste dumping") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Button(
            onClick = {
                if (alertText.isNotEmpty()) {
                    vm.sendAlert(alertText)
                    alertText = ""
                    Toast.makeText(context, "Alert Sent to Guardians!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F), contentColor = Color.White)
        ) {
            Text("SUBMIT URGENT ALERT")
        }
    }
}
