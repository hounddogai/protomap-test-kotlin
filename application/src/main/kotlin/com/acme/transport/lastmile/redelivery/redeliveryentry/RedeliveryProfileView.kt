package com.acme.transport.lastmile.redelivery.redeliveryentry

/** How screens and exports show a redelivery profile. */
data class RedeliveryProfileView(
    var codeText: String,
    var nameText: String,
    var routeCode: String,
    var budget: String,
    var checksum: String,
    var region: String,
    var shiftCodeText: String,
    var floorValue: String,
    var retries: String,
    var score: String,
    var statusLabel: String,
)

fun toRedeliveryProfileView(item: RedeliveryProfile): RedeliveryProfileView {
    return RedeliveryProfileView(
        codeText = item.code,
        nameText = item.name,
        routeCode = item.routeCode,
        budget = "%.2f".format(item.budget),
        checksum = item.checksum,
        region = item.region,
        shiftCodeText = item.shiftCode,
        floorValue = "%.2f".format(item.floorValue),
        retries = item.retries.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryProfileViews(items: MutableList<RedeliveryProfile>): MutableList<RedeliveryProfileView> {
    val views = mutableListOf<RedeliveryProfileView>()
    for (item in items) {
        views.add(toRedeliveryProfileView(item))
    }
    return views
}

fun sumRedeliveryProfileView(view: RedeliveryProfileView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedBudget + parsedFloorValue + parsedRetries.toDouble()
}
