package com.acme.transport.safety.violation.proposal

/** How screens and exports show a pending violation rollup. */
data class PendingViolationRollupView(
    var code: String,
    var nameText: String,
    var attemptsText: String,
    var limit: String,
    var priority: String,
    var statusLabel: String,
)

fun toPendingViolationRollupView(item: PendingViolationRollup): PendingViolationRollupView {
    return PendingViolationRollupView(
        code = item.code,
        nameText = item.name,
        attemptsText = item.attempts.toString(),
        limit = item.limit.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingViolationRollupViews(items: MutableList<PendingViolationRollup>): MutableList<PendingViolationRollupView> {
    val views = mutableListOf<PendingViolationRollupView>()
    for (item in items) {
        views.add(toPendingViolationRollupView(item))
    }
    return views
}

fun sumPendingViolationRollupView(view: PendingViolationRollupView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedAttempts.toDouble() + parsedLimit.toDouble() + parsedPriority.toDouble()
}
