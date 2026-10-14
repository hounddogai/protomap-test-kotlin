package com.acme.transport.safety.nearmiss.rule

/** How screens and exports show a near miss manifest. */
data class NearMissManifestView(
    var codeText: String,
    var nameText: String,
    var description: String,
    var windowDays: String,
    var margin: String,
    var maximum: String,
    var statusLabel: String,
)

fun toNearMissManifestView(item: NearMissManifest): NearMissManifestView {
    return NearMissManifestView(
        codeText = item.code,
        nameText = item.name,
        description = item.description,
        windowDays = item.windowDays.toString(),
        margin = "%.2f".format(item.margin),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissManifestViews(items: MutableList<NearMissManifest>): MutableList<NearMissManifestView> {
    val views = mutableListOf<NearMissManifestView>()
    for (item in items) {
        views.add(toNearMissManifestView(item))
    }
    return views
}

fun sumNearMissManifestView(view: NearMissManifestView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWindowDays.toDouble() + parsedMargin + parsedMaximum
}
