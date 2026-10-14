package com.acme.transport.fleetops.tire.tirewindow

/** How screens and exports show a tiered tire window. */
data class TieredTireWindowView(
    var code: String,
    var labelText: String,
    var approved: String,
    var contactEmail: String,
    var country: String,
    var score: String,
    var statusLabel: String,
)

fun toTieredTireWindowView(item: TieredTireWindow): TieredTireWindowView {
    return TieredTireWindowView(
        code = item.code,
        labelText = item.label,
        approved = "${item.approved}",
        contactEmail = item.contactEmail,
        country = item.country,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTireWindowViews(items: MutableList<TieredTireWindow>): MutableList<TieredTireWindowView> {
    val views = mutableListOf<TieredTireWindowView>()
    for (item in items) {
        views.add(toTieredTireWindowView(item))
    }
    return views
}

fun sumTieredTireWindowView(view: TieredTireWindowView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
