package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel profile. */
data class ParcelProfileView(
    var code: String,
    var name: String,
    var routeCode: String,
    var stopCount: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toParcelProfileView(item: ParcelProfile): ParcelProfileView {
    return ParcelProfileView(
        code = item.code,
        name = truncate(item.name, 40),
        routeCode = item.routeCode,
        stopCount = item.stopCount.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelProfileViews(items: MutableList<ParcelProfile>): MutableList<ParcelProfileView> {
    val views = mutableListOf<ParcelProfileView>()
    for (item in items) {
        views.add(toParcelProfileView(item))
    }
    return views
}

fun sumParcelProfileView(view: ParcelProfileView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedStopCount.toDouble() + parsedEtaMinutes.toDouble()
}
