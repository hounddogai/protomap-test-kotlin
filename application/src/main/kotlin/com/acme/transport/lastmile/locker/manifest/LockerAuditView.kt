package com.acme.transport.lastmile.locker.manifest

/** How screens and exports show a locker audit. */
data class LockerAuditView(
    var codeText: String,
    var name: String,
    var weight: String,
    var driverIdText: String,
    var channel: String,
    var vehicleId: String,
    var visibleText: String,
    var updatedAt: String,
    var cost: String,
    var statusLabel: String,
)

fun toLockerAuditView(item: LockerAudit): LockerAuditView {
    return LockerAuditView(
        codeText = item.code,
        name = item.name,
        weight = "%.2f".format(item.weight),
        driverIdText = item.driverId,
        channel = item.channel,
        vehicleId = item.vehicleId,
        visibleText = "${item.visible}",
        updatedAt = item.updatedAt.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerAuditViews(items: MutableList<LockerAudit>): MutableList<LockerAuditView> {
    val views = mutableListOf<LockerAuditView>()
    for (item in items) {
        views.add(toLockerAuditView(item))
    }
    return views
}

fun sumLockerAuditView(view: LockerAuditView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedWeight + parsedUpdatedAt.toDouble() + parsedCost
}
