package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global fuel surcharge batch. */
data class GlobalFuelSurchargeBatchView(
    var code: String,
    var name: String,
    var routeCode: String,
    var depot: String,
    var retries: String,
    var ratio: String,
    var cost: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toGlobalFuelSurchargeBatchView(item: GlobalFuelSurchargeBatch): GlobalFuelSurchargeBatchView {
    return GlobalFuelSurchargeBatchView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        routeCode = item.routeCode,
        depot = truncate(item.depot, 80),
        retries = item.retries.toString(),
        ratio = "%.2f".format(item.ratio),
        cost = "%.2f".format(item.cost),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelSurchargeBatchViews(items: MutableList<GlobalFuelSurchargeBatch>): MutableList<GlobalFuelSurchargeBatchView> {
    val views = mutableListOf<GlobalFuelSurchargeBatchView>()
    for (item in items) {
        views.add(toGlobalFuelSurchargeBatchView(item))
    }
    return views
}

fun sumGlobalFuelSurchargeBatchView(view: GlobalFuelSurchargeBatchView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRetries.toDouble() + parsedRatio + parsedCost
}
