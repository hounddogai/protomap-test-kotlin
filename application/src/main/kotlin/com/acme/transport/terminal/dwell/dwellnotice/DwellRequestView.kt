package com.acme.transport.terminal.dwell.dwellnotice

/** How screens and exports show a dwell request. */
data class DwellRequestView(
    var code: String,
    var title: String,
    var maximum: String,
    var sequence: String,
    var capacity: String,
    var unit: String,
    var scoreText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toDwellRequestView(item: DwellRequest): DwellRequestView {
    return DwellRequestView(
        code = item.code,
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        sequence = item.sequence.toString(),
        capacity = item.capacity.toString(),
        unit = item.unit,
        scoreText = "%.2f".format(item.score),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellRequestViews(items: MutableList<DwellRequest>): MutableList<DwellRequestView> {
    val views = mutableListOf<DwellRequestView>()
    for (item in items) {
        views.add(toDwellRequestView(item))
    }
    return views
}

fun sumDwellRequestView(view: DwellRequestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedMaximum + parsedSequence.toDouble() + parsedCapacity.toDouble()
}
