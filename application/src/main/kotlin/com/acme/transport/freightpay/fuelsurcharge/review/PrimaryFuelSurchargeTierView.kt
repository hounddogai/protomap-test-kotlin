package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary fuel surcharge tier. */
data class PrimaryFuelSurchargeTierView(
    var code: String,
    var label: String,
    var retries: String,
    var cost: String,
    var pallets: String,
    var expiresAt: String,
    var name: String,
    var statusLabel: String,
)

fun toPrimaryFuelSurchargeTierView(item: PrimaryFuelSurchargeTier): PrimaryFuelSurchargeTierView {
    return PrimaryFuelSurchargeTierView(
        code = truncate(item.code, 40),
        label = item.label,
        retries = item.retries.toString(),
        cost = "%.2f".format(item.cost),
        pallets = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelSurchargeTierViews(items: MutableList<PrimaryFuelSurchargeTier>): MutableList<PrimaryFuelSurchargeTierView> {
    val views = mutableListOf<PrimaryFuelSurchargeTierView>()
    for (item in items) {
        views.add(toPrimaryFuelSurchargeTierView(item))
    }
    return views
}

fun sumPrimaryFuelSurchargeTierView(view: PrimaryFuelSurchargeTierView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedRetries.toDouble() + parsedCost + parsedPallets.toDouble()
}
