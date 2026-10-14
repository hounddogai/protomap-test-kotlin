package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone profile. */
data class GlobalZoneProfileView(
    var codeText: String,
    var title: String,
    var rate: String,
    var segmentText: String,
    var carrierText: String,
    var capacityText: String,
    var threshold: String,
    var maximum: String,
    var discount: String,
    var versionText: String,
    var activeText: String,
    var statusLabel: String,
)

fun toGlobalZoneProfileView(item: GlobalZoneProfile): GlobalZoneProfileView {
    return GlobalZoneProfileView(
        codeText = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        segmentText = item.segment,
        carrierText = truncate(item.carrier, 80),
        capacityText = item.capacity.toString(),
        threshold = "%.2f".format(item.threshold),
        maximum = "%.2f".format(item.maximum),
        discount = "%.2f".format(item.discount),
        versionText = item.version.toString(),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneProfileViews(items: MutableList<GlobalZoneProfile>): MutableList<GlobalZoneProfileView> {
    val views = mutableListOf<GlobalZoneProfileView>()
    for (item in items) {
        views.add(toGlobalZoneProfileView(item))
    }
    return views
}

fun sumGlobalZoneProfileView(view: GlobalZoneProfileView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRate + parsedCapacity.toDouble() + parsedThreshold
}
