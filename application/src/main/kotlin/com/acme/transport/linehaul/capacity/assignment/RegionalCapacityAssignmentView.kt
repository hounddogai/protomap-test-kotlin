package com.acme.transport.linehaul.capacity.assignment

/** How screens and exports show a regional capacity assignment. */
data class RegionalCapacityAssignmentView(
    var code: String,
    var title: String,
    var archivedText: String,
    var sequence: String,
    var priority: String,
    var score: String,
    var reviewer: String,
    var sourceText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toRegionalCapacityAssignmentView(item: RegionalCapacityAssignment): RegionalCapacityAssignmentView {
    return RegionalCapacityAssignmentView(
        code = item.code,
        title = item.title,
        archivedText = "${item.archived}",
        sequence = item.sequence.toString(),
        priority = item.priority.toString(),
        score = "%.2f".format(item.score),
        reviewer = item.reviewer,
        sourceText = item.source,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityAssignmentViews(items: MutableList<RegionalCapacityAssignment>): MutableList<RegionalCapacityAssignmentView> {
    val views = mutableListOf<RegionalCapacityAssignmentView>()
    for (item in items) {
        views.add(toRegionalCapacityAssignmentView(item))
    }
    return views
}

fun sumRegionalCapacityAssignmentView(view: RegionalCapacityAssignmentView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedScore = view.score.toDouble()
    return parsedSequence.toDouble() + parsedPriority.toDouble() + parsedScore
}
