package com.acme.transport.customs.clearance.clearancemanifest

/** How screens and exports show a primary clearance manifest. */
data class PrimaryClearanceManifestView(
    var code: String,
    var titleText: String,
    var carrier: String,
    var capacity: String,
    var minimumText: String,
    var maximum: String,
    var versionText: String,
    var statusLabel: String,
)

fun toPrimaryClearanceManifestView(item: PrimaryClearanceManifest): PrimaryClearanceManifestView {
    return PrimaryClearanceManifestView(
        code = item.code,
        titleText = item.title,
        carrier = item.carrier,
        capacity = item.capacity.toString(),
        minimumText = "%.2f".format(item.minimum),
        maximum = "%.2f".format(item.maximum),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryClearanceManifestViews(items: MutableList<PrimaryClearanceManifest>): MutableList<PrimaryClearanceManifestView> {
    val views = mutableListOf<PrimaryClearanceManifestView>()
    for (item in items) {
        views.add(toPrimaryClearanceManifestView(item))
    }
    return views
}

fun sumPrimaryClearanceManifestView(view: PrimaryClearanceManifestView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCapacity.toDouble() + parsedMinimum + parsedMaximum
}
