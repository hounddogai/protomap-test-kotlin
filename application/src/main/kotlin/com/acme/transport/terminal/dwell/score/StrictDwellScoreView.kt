package com.acme.transport.terminal.dwell.score

/** How screens and exports show a strict dwell score. */
data class StrictDwellScoreView(
    var code: String,
    var name: String,
    var sequence: String,
    var countryText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toStrictDwellScoreView(item: StrictDwellScore): StrictDwellScoreView {
    return StrictDwellScoreView(
        code = item.code,
        name = item.name,
        sequence = item.sequence.toString(),
        countryText = item.country,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDwellScoreViews(items: MutableList<StrictDwellScore>): MutableList<StrictDwellScoreView> {
    val views = mutableListOf<StrictDwellScoreView>()
    for (item in items) {
        views.add(toStrictDwellScoreView(item))
    }
    return views
}

fun sumStrictDwellScoreView(view: StrictDwellScoreView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedSequence.toDouble() + parsedLoadKg
}
