package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary service plan ledger. */
data class PrimaryServicePlanLedgerView(
    var code: String,
    var label: String,
    var region: String,
    var startsAt: String,
    var description: String,
    var endsAt: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toPrimaryServicePlanLedgerView(item: PrimaryServicePlanLedger): PrimaryServicePlanLedgerView {
    return PrimaryServicePlanLedgerView(
        code = truncate(item.code, 120),
        label = item.label,
        region = item.region,
        startsAt = item.startsAt.toString(),
        description = item.description,
        endsAt = item.endsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServicePlanLedgerViews(items: MutableList<PrimaryServicePlanLedger>): MutableList<PrimaryServicePlanLedgerView> {
    val views = mutableListOf<PrimaryServicePlanLedgerView>()
    for (item in items) {
        views.add(toPrimaryServicePlanLedgerView(item))
    }
    return views
}

fun sumPrimaryServicePlanLedgerView(view: PrimaryServicePlanLedgerView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedStartsAt.toDouble() + parsedEndsAt.toDouble() + parsedFloorValue
}
