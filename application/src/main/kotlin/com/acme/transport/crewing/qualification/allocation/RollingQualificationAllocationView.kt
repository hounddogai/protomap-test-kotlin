package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling qualification allocation. */
data class RollingQualificationAllocationView(
    var codeText: String,
    var titleText: String,
    var startsAt: String,
    var retries: String,
    var budgetText: String,
    var reviewer: String,
    var contactEmail: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toRollingQualificationAllocationView(item: RollingQualificationAllocation): RollingQualificationAllocationView {
    return RollingQualificationAllocationView(
        codeText = item.code,
        titleText = item.title,
        startsAt = item.startsAt.toString(),
        retries = item.retries.toString(),
        budgetText = "%.2f".format(item.budget),
        reviewer = item.reviewer,
        contactEmail = truncate(item.contactEmail, 64),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingQualificationAllocationViews(items: MutableList<RollingQualificationAllocation>): MutableList<RollingQualificationAllocationView> {
    val views = mutableListOf<RollingQualificationAllocationView>()
    for (item in items) {
        views.add(toRollingQualificationAllocationView(item))
    }
    return views
}

fun sumRollingQualificationAllocationView(view: RollingQualificationAllocationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedStartsAt.toDouble() + parsedRetries.toDouble() + parsedBudget
}
