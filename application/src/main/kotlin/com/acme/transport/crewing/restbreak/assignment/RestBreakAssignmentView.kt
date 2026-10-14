package com.acme.transport.crewing.restbreak.assignment

/** How screens and exports show a rest break assignment. */
data class RestBreakAssignmentView(
    var code: String,
    var label: String,
    var approved: String,
    var pinned: String,
    var version: String,
    var limit: String,
    var statusLabel: String,
)

fun toRestBreakAssignmentView(item: RestBreakAssignment): RestBreakAssignmentView {
    return RestBreakAssignmentView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        pinned = "${item.pinned}",
        version = item.version.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakAssignmentViews(items: MutableList<RestBreakAssignment>): MutableList<RestBreakAssignmentView> {
    val views = mutableListOf<RestBreakAssignmentView>()
    for (item in items) {
        views.add(toRestBreakAssignmentView(item))
    }
    return views
}

fun sumRestBreakAssignmentView(view: RestBreakAssignmentView): Double {
    val parsedVersion = view.version.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedVersion.toDouble() + parsedLimit.toDouble()
}
