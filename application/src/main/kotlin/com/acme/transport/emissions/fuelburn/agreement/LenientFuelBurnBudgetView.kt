package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient fuel burn budget. */
data class LenientFuelBurnBudgetView(
    var code: String,
    var name: String,
    var sequence: String,
    var titleText: String,
    var expiresAt: String,
    var ownerId: String,
    var externalRefText: String,
    var limit: String,
    var weight: String,
    var checksum: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toLenientFuelBurnBudgetView(item: LenientFuelBurnBudget): LenientFuelBurnBudgetView {
    return LenientFuelBurnBudgetView(
        code = item.code,
        name = truncate(item.name, 80),
        sequence = item.sequence.toString(),
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        ownerId = truncate(item.ownerId, 120),
        externalRefText = item.externalRef,
        limit = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        checksum = item.checksum,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFuelBurnBudgetViews(items: MutableList<LenientFuelBurnBudget>): MutableList<LenientFuelBurnBudgetView> {
    val views = mutableListOf<LenientFuelBurnBudgetView>()
    for (item in items) {
        views.add(toLenientFuelBurnBudgetView(item))
    }
    return views
}

fun sumLenientFuelBurnBudgetView(view: LenientFuelBurnBudgetView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedSequence.toDouble() + parsedExpiresAt.toDouble() + parsedLimit.toDouble()
}
