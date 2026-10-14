package com.acme.transport.terminal.dockdoor.score

/** How screens and exports show a dock door score. */
data class DockDoorScoreView(
    var code: String,
    var name: String,
    var country: String,
    var graceDays: String,
    var timezone: String,
    var contactEmail: String,
    var retries: String,
    var statusLabel: String,
)

fun toDockDoorScoreView(item: DockDoorScore): DockDoorScoreView {
    return DockDoorScoreView(
        code = item.code,
        name = item.name,
        country = item.country,
        graceDays = item.graceDays.toString(),
        timezone = item.timezone,
        contactEmail = item.contactEmail,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorScoreViews(items: MutableList<DockDoorScore>): MutableList<DockDoorScoreView> {
    val views = mutableListOf<DockDoorScoreView>()
    for (item in items) {
        views.add(toDockDoorScoreView(item))
    }
    return views
}

fun sumDockDoorScoreView(view: DockDoorScoreView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedGraceDays.toDouble() + parsedRetries.toDouble()
}
