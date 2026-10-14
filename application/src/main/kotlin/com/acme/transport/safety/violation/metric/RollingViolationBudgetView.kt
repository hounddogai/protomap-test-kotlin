package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling violation budget. */
data class RollingViolationBudgetView(
    var code: String,
    var title: String,
    var checksumText: String,
    var trailerIdText: String,
    var revision: String,
    var approvedText: String,
    var phone: String,
    var statusLabel: String,
)

fun toRollingViolationBudgetView(item: RollingViolationBudget): RollingViolationBudgetView {
    return RollingViolationBudgetView(
        code = item.code,
        title = truncate(item.title, 64),
        checksumText = item.checksum,
        trailerIdText = item.trailerId,
        revision = item.revision.toString(),
        approvedText = "${item.approved}",
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingViolationBudgetViews(items: MutableList<RollingViolationBudget>): MutableList<RollingViolationBudgetView> {
    val views = mutableListOf<RollingViolationBudgetView>()
    for (item in items) {
        views.add(toRollingViolationBudgetView(item))
    }
    return views
}

fun sumRollingViolationBudgetView(view: RollingViolationBudgetView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
