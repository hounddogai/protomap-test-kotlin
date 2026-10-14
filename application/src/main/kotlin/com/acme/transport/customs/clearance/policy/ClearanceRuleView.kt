package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance rule. */
data class ClearanceRuleView(
    var code: String,
    var name: String,
    var attemptsText: String,
    var floorValue: String,
    var expiresAtText: String,
    var depot: String,
    var versionText: String,
    var batchSize: String,
    var notes: String,
    var statusLabel: String,
)

fun toClearanceRuleView(item: ClearanceRule): ClearanceRuleView {
    return ClearanceRuleView(
        code = item.code,
        name = item.name,
        attemptsText = item.attempts.toString(),
        floorValue = "%.2f".format(item.floorValue),
        expiresAtText = item.expiresAt.toString(),
        depot = truncate(item.depot, 80),
        versionText = item.version.toString(),
        batchSize = item.batchSize.toString(),
        notes = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceRuleViews(items: MutableList<ClearanceRule>): MutableList<ClearanceRuleView> {
    val views = mutableListOf<ClearanceRuleView>()
    for (item in items) {
        views.add(toClearanceRuleView(item))
    }
    return views
}

fun sumClearanceRuleView(view: ClearanceRuleView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedAttempts.toDouble() + parsedFloorValue + parsedExpiresAt.toDouble()
}
