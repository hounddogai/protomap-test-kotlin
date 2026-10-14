package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual parcel mapping. */
data class ManualParcelMappingView(
    var codeText: String,
    var name: String,
    var durationMinutesText: String,
    var windowDays: String,
    var approvedText: String,
    var country: String,
    var trailerId: String,
    var checksumText: String,
    var locale: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toManualParcelMappingView(item: ManualParcelMapping): ManualParcelMappingView {
    return ManualParcelMappingView(
        codeText = item.code,
        name = item.name,
        durationMinutesText = item.durationMinutes.toString(),
        windowDays = item.windowDays.toString(),
        approvedText = "${item.approved}",
        country = item.country,
        trailerId = item.trailerId,
        checksumText = item.checksum,
        locale = truncate(item.locale, 120),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualParcelMappingViews(items: MutableList<ManualParcelMapping>): MutableList<ManualParcelMappingView> {
    val views = mutableListOf<ManualParcelMappingView>()
    for (item in items) {
        views.add(toManualParcelMappingView(item))
    }
    return views
}

fun sumManualParcelMappingView(view: ManualParcelMappingView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedDurationMinutes.toDouble() + parsedWindowDays.toDouble()
}
