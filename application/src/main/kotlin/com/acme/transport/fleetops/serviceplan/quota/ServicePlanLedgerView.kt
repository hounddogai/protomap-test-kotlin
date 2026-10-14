package com.acme.transport.fleetops.serviceplan.quota

/** How screens and exports show a service plan ledger. */
data class ServicePlanLedgerView(
    var code: String,
    var label: String,
    var updatedAt: String,
    var ceiling: String,
    var driverId: String,
    var statusLabel: String,
)

fun toServicePlanLedgerView(item: ServicePlanLedger): ServicePlanLedgerView {
    return ServicePlanLedgerView(
        code = item.code,
        label = item.label,
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanLedgerViews(items: MutableList<ServicePlanLedger>): MutableList<ServicePlanLedgerView> {
    val views = mutableListOf<ServicePlanLedgerView>()
    for (item in items) {
        views.add(toServicePlanLedgerView(item))
    }
    return views
}

fun sumServicePlanLedgerView(view: ServicePlanLedgerView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedUpdatedAt.toDouble() + parsedCeiling
}
