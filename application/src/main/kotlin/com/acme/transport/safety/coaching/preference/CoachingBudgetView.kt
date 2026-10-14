package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching budget. */
data class CoachingBudgetView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var windowDays: String,
    var contactEmailText: String,
    var updatedAtText: String,
    var threshold: String,
    var localeText: String,
    var budgetText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toCoachingBudgetView(item: CoachingBudget): CoachingBudgetView {
    return CoachingBudgetView(
        code = item.code,
        label = truncate(item.label, 64),
        expiresAt = item.expiresAt.toString(),
        windowDays = item.windowDays.toString(),
        contactEmailText = item.contactEmail,
        updatedAtText = item.updatedAt.toString(),
        threshold = "%.2f".format(item.threshold),
        localeText = item.locale,
        budgetText = "%.2f".format(item.budget),
        ownerId = truncate(item.ownerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingBudgetViews(items: MutableList<CoachingBudget>): MutableList<CoachingBudgetView> {
    val views = mutableListOf<CoachingBudgetView>()
    for (item in items) {
        views.add(toCoachingBudgetView(item))
    }
    return views
}

fun sumCoachingBudgetView(view: CoachingBudgetView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedExpiresAt.toDouble() + parsedWindowDays.toDouble() + parsedUpdatedAt.toDouble()
}
