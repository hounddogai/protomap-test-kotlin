package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional defect allocation. */
data class RegionalDefectAllocationView(
    var code: String,
    var nameText: String,
    var reviewer: String,
    var carrier: String,
    var country: String,
    var checksumText: String,
    var revision: String,
    var odometerText: String,
    var source: String,
    var statusLabel: String,
)

fun toRegionalDefectAllocationView(item: RegionalDefectAllocation): RegionalDefectAllocationView {
    return RegionalDefectAllocationView(
        code = item.code,
        nameText = truncate(item.name, 40),
        reviewer = item.reviewer,
        carrier = item.carrier,
        country = item.country,
        checksumText = item.checksum,
        revision = item.revision.toString(),
        odometerText = item.odometer.toString(),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDefectAllocationViews(items: MutableList<RegionalDefectAllocation>): MutableList<RegionalDefectAllocationView> {
    val views = mutableListOf<RegionalDefectAllocationView>()
    for (item in items) {
        views.add(toRegionalDefectAllocationView(item))
    }
    return views
}

fun sumRegionalDefectAllocationView(view: RegionalDefectAllocationView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedRevision.toDouble() + parsedOdometer.toDouble()
}
