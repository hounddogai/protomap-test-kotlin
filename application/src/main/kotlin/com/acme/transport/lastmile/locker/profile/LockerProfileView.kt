package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker profile. */
data class LockerProfileView(
    var code: String,
    var nameText: String,
    var driverId: String,
    var vehicleId: String,
    var margin: String,
    var versionText: String,
    var lane: String,
    var externalRef: String,
    var minimumText: String,
    var fuelLitersText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toLockerProfileView(item: LockerProfile): LockerProfileView {
    return LockerProfileView(
        code = truncate(item.code, 64),
        nameText = item.name,
        driverId = truncate(item.driverId, 80),
        vehicleId = item.vehicleId,
        margin = "%.2f".format(item.margin),
        versionText = item.version.toString(),
        lane = item.lane,
        externalRef = item.externalRef,
        minimumText = "%.2f".format(item.minimum),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerProfileViews(items: MutableList<LockerProfile>): MutableList<LockerProfileView> {
    val views = mutableListOf<LockerProfileView>()
    for (item in items) {
        views.add(toLockerProfileView(item))
    }
    return views
}

fun sumLockerProfileView(view: LockerProfileView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMargin + parsedVersion.toDouble() + parsedMinimum
}
