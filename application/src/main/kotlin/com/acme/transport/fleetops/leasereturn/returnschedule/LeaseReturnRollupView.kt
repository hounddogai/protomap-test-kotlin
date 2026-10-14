package com.acme.transport.fleetops.leasereturn.returnschedule

/** How screens and exports show a lease return rollup. */
data class LeaseReturnRollupView(
    var code: String,
    var nameText: String,
    var carrierText: String,
    var title: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLeaseReturnRollupView(item: LeaseReturnRollup): LeaseReturnRollupView {
    return LeaseReturnRollupView(
        code = item.code,
        nameText = item.name,
        carrierText = item.carrier,
        title = item.title,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnRollupViews(items: MutableList<LeaseReturnRollup>): MutableList<LeaseReturnRollupView> {
    val views = mutableListOf<LeaseReturnRollupView>()
    for (item in items) {
        views.add(toLeaseReturnRollupView(item))
    }
    return views
}

fun sumLeaseReturnRollupView(view: LeaseReturnRollupView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
