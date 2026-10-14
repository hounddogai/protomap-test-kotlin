package com.acme.transport.lastmile.locker.cycle

/** How screens and exports show a locker summary. */
data class LockerSummaryView(
    var codeText: String,
    var nameText: String,
    var active: String,
    var score: String,
    var priority: String,
    var timezone: String,
    var routeCode: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toLockerSummaryView(item: LockerSummary): LockerSummaryView {
    return LockerSummaryView(
        codeText = item.code,
        nameText = item.name,
        active = "${item.active}",
        score = "%.2f".format(item.score),
        priority = item.priority.toString(),
        timezone = item.timezone,
        routeCode = item.routeCode,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerSummaryViews(items: MutableList<LockerSummary>): MutableList<LockerSummaryView> {
    val views = mutableListOf<LockerSummaryView>()
    for (item in items) {
        views.add(toLockerSummaryView(item))
    }
    return views
}

fun sumLockerSummaryView(view: LockerSummaryView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedScore + parsedPriority.toDouble()
}
