package com.acme.transport.crewing.roster.forecast

/** How screens and exports show a roster manifest. */
data class RosterManifestView(
    var code: String,
    var title: String,
    var ratioText: String,
    var endsAtText: String,
    var odometer: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRosterManifestView(item: RosterManifest): RosterManifestView {
    return RosterManifestView(
        code = item.code,
        title = item.title,
        ratioText = "%.2f".format(item.ratio),
        endsAtText = item.endsAt.toString(),
        odometer = item.odometer.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterManifestViews(items: MutableList<RosterManifest>): MutableList<RosterManifestView> {
    val views = mutableListOf<RosterManifestView>()
    for (item in items) {
        views.add(toRosterManifestView(item))
    }
    return views
}

fun sumRosterManifestView(view: RosterManifestView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRatio + parsedEndsAt.toDouble() + parsedOdometer.toDouble()
}
