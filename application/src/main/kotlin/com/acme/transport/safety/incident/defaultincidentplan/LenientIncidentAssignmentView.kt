package com.acme.transport.safety.incident.defaultincidentplan

/** How screens and exports show a lenient incident assignment. */
data class LenientIncidentAssignmentView(
    var code: String,
    var label: String,
    var approved: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toLenientIncidentAssignmentView(item: LenientIncidentAssignment): LenientIncidentAssignmentView {
    return LenientIncidentAssignmentView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIncidentAssignmentViews(items: MutableList<LenientIncidentAssignment>): MutableList<LenientIncidentAssignmentView> {
    val views = mutableListOf<LenientIncidentAssignmentView>()
    for (item in items) {
        views.add(toLenientIncidentAssignmentView(item))
    }
    return views
}

fun sumLenientIncidentAssignmentView(view: LenientIncidentAssignmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
