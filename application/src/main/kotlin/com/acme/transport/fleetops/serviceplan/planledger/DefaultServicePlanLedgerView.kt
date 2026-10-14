package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default service plan ledger. */
data class DefaultServicePlanLedgerView(
    var code: String,
    var label: String,
    var region: String,
    var contactEmailText: String,
    var ceiling: String,
    var attempts: String,
    var startsAtText: String,
    var sequenceText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefaultServicePlanLedgerView(item: DefaultServicePlanLedger): DefaultServicePlanLedgerView {
    return DefaultServicePlanLedgerView(
        code = item.code,
        label = truncate(item.label, 80),
        region = item.region,
        contactEmailText = truncate(item.contactEmail, 80),
        ceiling = "%.2f".format(item.ceiling),
        attempts = item.attempts.toString(),
        startsAtText = item.startsAt.toString(),
        sequenceText = item.sequence.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultServicePlanLedgerViews(items: MutableList<DefaultServicePlanLedger>): MutableList<DefaultServicePlanLedgerView> {
    val views = mutableListOf<DefaultServicePlanLedgerView>()
    for (item in items) {
        views.add(toDefaultServicePlanLedgerView(item))
    }
    return views
}

fun sumDefaultServicePlanLedgerView(view: DefaultServicePlanLedgerView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedCeiling + parsedAttempts.toDouble() + parsedStartsAt.toDouble()
}
