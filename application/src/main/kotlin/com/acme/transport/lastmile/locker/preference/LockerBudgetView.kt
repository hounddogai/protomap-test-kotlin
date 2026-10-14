package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker budget. */
data class LockerBudgetView(
    var code: String,
    var nameText: String,
    var cost: String,
    var country: String,
    var createdAt: String,
    var checksum: String,
    var approvedText: String,
    var etaMinutes: String,
    var attemptsText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toLockerBudgetView(item: LockerBudget): LockerBudgetView {
    return LockerBudgetView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 64),
        cost = "%.2f".format(item.cost),
        country = item.country,
        createdAt = item.createdAt.toString(),
        checksum = item.checksum,
        approvedText = "${item.approved}",
        etaMinutes = item.etaMinutes.toString(),
        attemptsText = item.attempts.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerBudgetViews(items: MutableList<LockerBudget>): MutableList<LockerBudgetView> {
    val views = mutableListOf<LockerBudgetView>()
    for (item in items) {
        views.add(toLockerBudgetView(item))
    }
    return views
}

fun sumLockerBudgetView(view: LockerBudgetView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCost + parsedCreatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
