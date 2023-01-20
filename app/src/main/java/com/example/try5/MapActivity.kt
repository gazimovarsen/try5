package com.example.try5

//import android.location.LocationRequest
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.try5.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = MapActivity::class.java.simpleName

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
        setMapStyle(mMap)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location->
            if(location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)

                val userEmail = firebaseAuth.currentUser?.email.toString()
                val db = FirebaseFirestore.getInstance()
                val lt = location.latitude
                val lg = location.longitude

                var ocherednyara_name = "unknown"

                db.collection("users").get().addOnCompleteListener{
                    if(it.isSuccessful){
                        for(document in it.result!!){
                            if(document.data.getValue("email") == userEmail){
                                db.collection("users").document(document.id).update(
                                    mapOf(
                                        "latitude" to lt.toString(),
                                        "longitude" to lg.toString()
                                    )
                                )
                            }

                            var ocherednyara: LatLng
                            if((document.data.getValue("online") as Boolean) and !(document.data.getValue("email") == userEmail)){
                                ocherednyara = LatLng(document.getString("latitude")!!.toDouble(), document.getString("longitude")!!.toDouble())
                                ocherednyara_name = document.getString("name")!!
                                placeMarkerOnMap(ocherednyara, ocherednyara_name)
                            }
                        }
                    }
                }

                //placeMarkerOnMap(currentLatLong, ocherednyara_name)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng, currentName: String) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong, $currentName")
        mMap.addMarker(markerOptions)
    }

    override fun onPause() {
        goOffline()
        super.onPause()
    }

    override fun onDestroy() {
        goOffline()
        super.onDestroy()
    }

    override fun onResume() {
        goOnline()
        super.onResume()
    }

    override fun onRestart() {
        goOnline()
        super.onRestart()
    }

    override fun onMarkerClick(p0: Marker): Boolean = false

    private fun goOnline(){
        val db = FirebaseFirestore.getInstance()
        val userEmail = firebaseAuth.currentUser?.email.toString()
        db.collection("users").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(document in it.result!!){
                    if(document.data.getValue("email") == userEmail){
                        db.collection("users").document(document.id).update(
                            mapOf(
                                "online" to true
                            )
                        )
                    }
                }
            }
        }
    }

    private fun goOffline(){
        val db = FirebaseFirestore.getInstance()
        val userEmail = firebaseAuth.currentUser?.email.toString()
        db.collection("users").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(document in it.result!!){
                    if(document.data.getValue("email") == userEmail){
                        db.collection("users").document(document.id).update(
                            mapOf(
                                "online" to false
                            )
                        )
                    }
                }
            }
        }
    }

//    private fun showPeople(){
//        val db = FirebaseFirestore.getInstance()
//        db.collection("users").get().addOnCompleteListener{
//            if(it.isSuccessful){
//                for(document in it.result!!){
//                    if(document.data.getValue("online") as Boolean){
//                        val currentPerson = LatLng(document.data.getValue("latitude") as Double,
//                            document.data.getValue("longitude") as Double)
//                        document.getString("name")
//                            ?.let { it1 -> placeMarkerOnMap(currentPerson, it1) }
//                        Log.d("TAG", "person is on map")
//                    }
//                }
//            }
//        }
//    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
}