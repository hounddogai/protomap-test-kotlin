package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict locker budget. */
data class StrictLockerBudgetView(
    var code: String,
    var name: String,
    var reviewerText: String,
    var attemptsText: String,
    var amountText: String,
    var endsAt: String,
    var quantity: String,
    var archived: String,
    var graceDays: String,
    var limit: String,
    var statusLabel: String,
)

fun toStrictLockerBudgetView(item: StrictLockerBudget): StrictLockerBudgetView {
    return StrictLockerBudgetView(
        code = item.code,
        name = truncate(item.name, 80),
        reviewerText = item.reviewer,
        attemptsText = item.attempts.toString(),
        amountText = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        quantity = item.quantity.toString(),
        archived = "${item.archived}",
        graceDays = item.graceDays.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLockerBudgetViews(items: MutableList<StrictLockerBudget>): MutableList<StrictLockerBudgetView> {
    val views = mutableListOf<StrictLockerBudgetView>()
    for (item in items) {
        views.add(toStrictLockerBudgetView(item))
    }
    return views
}

fun sumStrictLockerBudgetView(view: StrictLockerBudgetView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedAttempts.toDouble() + parsedAmount + parsedEndsAt.toDouble()
}
