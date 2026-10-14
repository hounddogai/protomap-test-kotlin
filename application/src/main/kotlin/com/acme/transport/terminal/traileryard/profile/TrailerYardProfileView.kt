package com.acme.transport.terminal.traileryard.profile

/** How screens and exports show a trailer yard profile. */
data class TrailerYardProfileView(
    var codeText: String,
    var label: String,
    var updatedAt: String,
    var rate: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toTrailerYardProfileView(item: TrailerYardProfile): TrailerYardProfileView {
    return TrailerYardProfileView(
        codeText = item.code,
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        rate = "%.2f".format(item.rate),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardProfileViews(items: MutableList<TrailerYardProfile>): MutableList<TrailerYardProfileView> {
    val views = mutableListOf<TrailerYardProfileView>()
    for (item in items) {
        views.add(toTrailerYardProfileView(item))
    }
    return views
}

fun sumTrailerYardProfileView(view: TrailerYardProfileView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedUpdatedAt.toDouble() + parsedRate + parsedStopCount.toDouble()
}
