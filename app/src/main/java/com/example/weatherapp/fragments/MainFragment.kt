package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.*
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.model.WeatherModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject


class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val viewModel: MainViewModel by activityViewModels()
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tlist = listOf(
        "Hours",
        "Days"
    )





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        visibilityItem()
        checkPermission()
        init()
        requestWeatherData("Novosibirsk")
        updateCurrentCard()

        ibSearch.setOnClickListener {
            searchCity()
        }

        ibSearchCity.setOnClickListener{
            if(edSearchCity.text!= null){
                init()
                requestWeatherData(edSearchCity.text.toString())
                updateCurrentCard()
                visibilityItem()
            }

        }
        ibClose.setOnClickListener {
            visibilityItem()
        }
        ibUpdate.setOnClickListener {
            init()
            requestWeatherData(edSearchCity.text.toString())
            updateCurrentCard()
            visibilityItem()
        }
    }


    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding) {
        fLocationClient=LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tlist[pos]
        }.attach()
        ibLocation.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
    }


    private fun checkLocation(){
        if (isLocationEnabled()){
            getLocation()
        }
        else{
            DialogManager.locationSettingsDialog(requireContext(),object :DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }

    private fun isLocationEnabled():Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){
        if (!isLocationEnabled()){
            Toast.makeText(requireContext(),getString(R.string.GPS),Toast.LENGTH_SHORT).show()
            return
        }
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun visibilityItem() = with(binding) {
        ibSearch.visibility = View.VISIBLE
        tvCity.visibility = View.VISIBLE
        ibClose.visibility = View.GONE
        ibSearchCity.visibility = View.GONE
        edSearchCity.visibility = View.GONE
        tvCityName.visibility = View.GONE

    }

    private fun searchCity() = with(binding) {
        ibSearch.visibility = View.GONE
        tvCity.visibility = View.INVISIBLE
        ibClose.visibility = View.VISIBLE
        ibSearchCity.visibility = View.VISIBLE
        edSearchCity.visibility = View.VISIBLE
        edSearchCity.setText("")
    }


    private fun updateCurrentCard() = with(binding) {
        viewModel.liveDataCurrent.observe(viewLifecycleOwner) {
            val mimMaxTemp = "${it.minTemp}°С/${it.maxTemp}"
            val currentTemp = "${it.currentTemp}°С"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = "${it.currentTemp.ifEmpty { mimMaxTemp }}°С"
            tvCondition.text = it.condition
            tvMinMax.text = if (it.currentTemp.isEmpty())"" else mimMaxTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
        }
    }


    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        } else {
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                Log.d("Victory", "error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours
        )
        viewModel.liveDataCurrent.value = item
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        viewModel.liveDataList.value = list
        return list
    }

    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()
    }
}