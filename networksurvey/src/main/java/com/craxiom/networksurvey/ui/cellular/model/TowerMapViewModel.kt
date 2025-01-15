package com.craxiom.networksurvey.ui.cellular.model

import android.graphics.DashPathEffect
import android.location.Location
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.craxiom.networksurvey.model.CellularProtocol
import com.craxiom.networksurvey.model.CellularRecordWrapper
import com.craxiom.networksurvey.ui.ASignalChartViewModel
import com.craxiom.networksurvey.util.CellularUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import timber.log.Timber
import java.util.Objects

/**
 * The view model for the Tower Map screen.
 */
internal class TowerMapViewModel : ASignalChartViewModel() {

    private var _paddingInsets = MutableStateFlow(PaddingValues(0.dp, 0.dp, 0.dp, 0.dp))
    val paddingInsets = _paddingInsets.asStateFlow()

    private var _servingCells =
        MutableStateFlow<HashMap<Int, ServingCellInfo>>(HashMap()) // <SubscriptionId, ServingCellInfo>
    val servingCells = _servingCells.asStateFlow()

    val subIdToServingCellLocations = HashMap<Int, GeoPoint>()
    var myLocation: Location? = null

    private var _servingSignals =
        MutableStateFlow<HashMap<Int, ServingSignalInfo>>(HashMap()) // <SubscriptionId, ServingSignalInfo>
    val servingSignals = _servingSignals.asStateFlow()

    var mapView: MapView? = null
    lateinit var gpsMyLocationProvider: GpsMyLocationProvider
    private var hasMapLocationBeenSet = false

    var myLocationOverlay: CustomLocationOverlay? = null

    lateinit var towerOverlayGroup: RadiusMarkerClusterer
    val servingCellLinesOverlayGroup: FolderOverlay = FolderOverlay()

    private val _towers = MutableStateFlow(LinkedHashSet<TowerMarker>(LinkedHashSet()))
    val towers = _towers.asStateFlow()

    private val _noTowersFound = MutableStateFlow(false)
    val noTowersFound = _noTowersFound.asStateFlow()

    private val _selectedRadioType = MutableStateFlow(CellularProtocol.LTE.name)
    val selectedRadioType = _selectedRadioType.asStateFlow()

    private val _isLoadingInProgress = MutableStateFlow(true)
    val isLoadingInProgress = _isLoadingInProgress.asStateFlow()

    private val _isZoomedOutTooFar = MutableStateFlow(false)
    val isZoomedOutTooFar = _isZoomedOutTooFar.asStateFlow()

    private val _lastQueriedBounds = MutableStateFlow<BoundingBox?>(null)
    val lastQueriedBounds = _lastQueriedBounds.asStateFlow()

    private val _mapZoomLevel = MutableStateFlow(0.0)
    val mapZoomLevel = _mapZoomLevel.asStateFlow()

    fun setPaddingInsets(paddingValues: PaddingValues) {
        _paddingInsets.value = paddingValues
    }

    fun setNoTowersFound(noTowersFound: Boolean) {
        _noTowersFound.value = noTowersFound
    }

    fun setSelectedRadioType(radioType: String) {
        _selectedRadioType.value = radioType
    }

    fun setIsLoadingInProgress(isLoading: Boolean) {
        _isLoadingInProgress.value = isLoading
    }

    fun setIsZoomedOutTooFar(isZoomedOut: Boolean) {
        _isZoomedOutTooFar.value = isZoomedOut
    }

    fun setLastQueriedBounds(bounds: BoundingBox) {
        _lastQueriedBounds.value = bounds
    }

    fun setMapZoomLevel(zoomLevel: Double) {
        _mapZoomLevel.value = zoomLevel
    }

    /**
     * Sets the center of the map to the provided location.
     */
    fun setMapCenterLocation(location: Location?): Boolean {
        if (location != null && !hasMapLocationBeenSet) {
            if (location.latitude != 0.0 || location.longitude != 0.0) {
                hasMapLocationBeenSet = true
                mapView!!.controller.setCenter(GeoPoint(location.latitude, location.longitude))
            }
        }

        return hasMapLocationBeenSet
    }

    fun onCellularBatchResults(
        cellularBatchResults: MutableList<CellularRecordWrapper?>?,
        subscriptionId: Int
    ) {
        if (cellularBatchResults.isNullOrEmpty()) return

        // Get the servingCellRecord from the cellularBatchResults and add it to the servingCells map
        // If none are found then clear the serving cell map for that particular subscriptionId
        val servingCellRecord =
            cellularBatchResults.firstOrNull {
                it?.cellularRecord != null && CellularUtils.isServingCell(it.cellularRecord)
            }

        updateServingCellSignals(servingCellRecord, subscriptionId)

        // No need to update the serving cell if it is the same as the current serving cell. This
        // prevents a map refresh which is expensive.
        val currentServingCell = _servingCells.value[subscriptionId]
        if (Objects.equals(currentServingCell?.servingCell, servingCellRecord)) return

        if (servingCellRecord == null) {
            _servingCells.update { map ->
                map.remove(subscriptionId)
                map
            }
        } else {
            _servingCells.update { oldMap ->
                val newMap = HashMap(oldMap)
                newMap[subscriptionId] = ServingCellInfo(servingCellRecord, subscriptionId)
                newMap
            }
        }

        mapView?.let { mapView ->
            recreateOverlaysFromTowerData(mapView, false)
        }
    }

    /**
     * Triggers any necessary updates to SIM count aware variables.
     */
    fun resetSimCount() {
        _servingCells.update {
            it.clear()
            it
        }
        _servingSignals.update {
            it.clear()
            it
        }
    }

    /**
     * Recreates the overlays on the map based on the current tower data.
     * @param mapView The map view to add the overlays to.
     * @param invalidate True to invalidate the map view after adding the overlays. Invalidating the
     * map will trigger a redraw of the map, which is important if the towers have changed, but a
     * side effect is that it closes any open info windows (shown when a user clicks on a tower).
     */
    @Synchronized
    fun recreateOverlaysFromTowerData(mapView: MapView, invalidate: Boolean = true) {
        try {
            towerOverlayGroup.items?.clear()
            subIdToServingCellLocations.clear()

            val towers = towers.value
            val servingCellGciIds: List<String>
            val servingCellToSubscriptionMap =
                servingCells.value.entries.associate { entry ->
                    CellularUtils.getTowerId(entry.value) to entry.value.subscriptionId
                }
            servingCellToSubscriptionMap.let { servingCellGciIds = it.keys.toList() }

            Timber.i("Adding %s points to the map", towers.size)
            towers.forEach { marker ->
                val isServingCell = servingCellGciIds.contains(marker.cgiId)
                if (isServingCell) {
                    // Get the value form servingCellToSubscriptionMap to be the key for the
                    // subIdToServingCellLocations so that we can set the value as marker.position
                    subIdToServingCellLocations[servingCellToSubscriptionMap[marker.cgiId]!!] =
                        marker.position
                }
                marker.setServingCell(isServingCell)
                towerOverlayGroup.add(marker)
            }

            drawServingCellLine()

            // .clusterer can cause a NPE if the markers are changed while the map is being drawn
            towerOverlayGroup.clusterer(mapView)

            if (invalidate) {
                towerOverlayGroup.invalidate()
            }
            mapView.postInvalidate()
        } catch (e: Exception) {
            Timber.e(e, "Something went wrong while recreating the overlays on the map")
        }
    }

    /**
     * Draws a line between the current location and all the serving cell locations.
     */
    @Synchronized
    fun drawServingCellLine() {
        try {
            servingCellLinesOverlayGroup.items?.clear()

            val currentLocation = myLocation ?: return

            if (subIdToServingCellLocations.isEmpty()) return

            val myGeoPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)

            subIdToServingCellLocations.forEach { (_, geoPoint) ->
                val polyline = Polyline()
                polyline.outlinePaint.strokeWidth = 4f
                polyline.outlinePaint.setPathEffect(DashPathEffect(floatArrayOf(10f, 20f), 0f))
                servingCellLinesOverlayGroup.add(polyline)

                val pathPoints = ArrayList<GeoPoint>()
                pathPoints.add(myGeoPoint)
                pathPoints.add(geoPoint)
                polyline.setPoints(pathPoints)
            }
        } catch (e: Exception) {
            // Sometimes the servingCellLinesOverlayGroup will throw a NPE on the #add call because
            // the mOverlayManager is assigned null on cleanup
            Timber.e(e, "Something went wrong while drawing the serving cell lines on the map")
        }
    }

    private fun updateServingCellSignals(
        servingCellRecord: CellularRecordWrapper?,
        subscriptionId: Int
    ) {
        if (servingCellRecord == null) {
            _servingSignals.update { map ->
                map.remove(subscriptionId)
                map
            }
        } else {
            _servingSignals.update { oldMap ->
                val newMap = HashMap(oldMap)
                newMap[subscriptionId] = CellularUtils.getSignalInfo(servingCellRecord)
                newMap
            }
        }
    }
}
