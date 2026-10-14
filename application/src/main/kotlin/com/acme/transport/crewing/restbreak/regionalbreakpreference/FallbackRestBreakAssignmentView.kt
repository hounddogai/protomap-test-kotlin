package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback rest break assignment. */
data class FallbackRestBreakAssignmentView(
    var code: String,
    var name: String,
    var minimum: String,
    var revision: String,
    var rateText: String,
    var reviewer: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toFallbackRestBreakAssignmentView(item: FallbackRestBreakAssignment): FallbackRestBreakAssignmentView {
    return FallbackRestBreakAssignmentView(
        code = item.code,
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        revision = item.revision.toString(),
        rateText = "%.2f".format(item.rate),
        reviewer = truncate(item.reviewer, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRestBreakAssignmentViews(items: MutableList<FallbackRestBreakAssignment>): MutableList<FallbackRestBreakAssignmentView> {
    val views = mutableListOf<FallbackRestBreakAssignmentView>()
    for (item in items) {
        views.add(toFallbackRestBreakAssignmentView(item))
    }
    return views
}

fun sumFallbackRestBreakAssignmentView(view: FallbackRestBreakAssignmentView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedMinimum + parsedRevision.toDouble() + parsedRate
}
