package com.example.devarakadu

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroveViewModel : ViewModel() {
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // This holds the list of Sacred Groves for the "Grove Directory"
    private val _groves = MutableStateFlow<List<Grove>>(emptyList())
    val groves = _groves.asStateFlow()

    // Species database for the "Species Scan" feature
    val speciesList = listOf(
        Species("Wild Orchid", "Aerides crispa", "A beautiful epiphytic orchid found in sacred groves.", "Considered a gift from the forest deity."),
        Species("Malabar Ironwood", "Hopea parviflora", "A tall tree endemic to the Western Ghats.", "Commonly protected in Devara Kadus as a sacred pillar."),
        Species("Giant Fern", "Angiopteris evecta", "A primitive fern that thrives in the humid micro-climate of groves.", "Indicates the ancient nature of the forest.")
    )

    private val _scannedSpecies = MutableStateFlow<Species?>(null)
    val scannedSpecies = _scannedSpecies.asStateFlow()

    private val _currentLocation = MutableStateFlow<MyLatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    init {
        fetchGroves()
    }

    fun updateLocation(latLng: MyLatLng) {
        _currentLocation.value = latLng
    }

    fun isNear(grove: Grove, current: MyLatLng?): Boolean {
        if (current == null) return false
        // Simple distance check (approximate for demo)
        val threshold = 0.05 // Roughly 5km
        val distance = Math.sqrt(Math.pow(current.latitude - grove.latitude, 2.0) + Math.pow(current.longitude - grove.longitude, 2.0))
        return distance < threshold
    }

    fun scanSpecies() {
        // Simulating a scan by picking a random species
        _scannedSpecies.value = speciesList.random()
    }

    private fun fetchGroves() {
        val mockData = listOf(
            Grove("1", "Irupu Devara Kadu", "Devara Kaadu", "Lord Shiva", "Legend says the river Lakshmana Tirtha was created by an arrow shot by Lakshmana.", "Crucial watershed area for the Lakshmana Tirtha river, supporting rare riparian flora.", 12.033, 75.966),
            Grove("2", "Talakaveri Forest", "Bana", "Goddess Kaveri", "The birthplace of River Kaveri, where Agastya Muni meditated.", "High-altitude evergreen forest acting as a massive carbon sink and soil stabilizer.", 12.387, 75.489),
            Grove("3", "Kukke Serpent Grove", "Nagabana", "Lord Subramanya", "A protected sanctuary for snakes where they are worshipped as divine guardians.", "Preserves the micro-habitat necessary for King Cobras and other reptiles.", 12.663, 75.591),
            Grove("4", "Puthari Kaavu", "Kaavu", "Village Deity", "The venue for the annual harvest festival, celebrating the sacred link between earth and man.", "Refuge for medicinal plants like wild ginger and turmeric used in traditional healing.", 12.124, 75.789),
            Grove("5", "Village Community Woods", "Gundutope", "Nature Spirit", "Traditional community-managed woodlots meant for shade and spiritual gathering.", "Acts as a local 'cooling lung' for the village, reducing ambient temperatures.", 12.456, 75.812)
        )

        val database = db
        if (database == null) {
            _groves.value = mockData
            return
        }

        database.collection("groves").addSnapshotListener { snapshot, _ ->
            val items = snapshot?.toObjects(Grove::class.java) ?: emptyList()
            _groves.value = if (items.isEmpty()) mockData else items
        }
    }

    // This function handles the "Conservation Alert" from the project sheet
    fun sendAlert(description: String) {
        val alert = hashMapOf(
            "report" to description,
            "timestamp" to System.currentTimeMillis(),
        )
        val database = db
        database?.collection("alerts")?.add(alert)
    }
}