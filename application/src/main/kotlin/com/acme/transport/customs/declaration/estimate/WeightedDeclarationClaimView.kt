package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted declaration claim. */
data class WeightedDeclarationClaimView(
    var code: String,
    var labelText: String,
    var channel: String,
    var visibleText: String,
    var driverId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toWeightedDeclarationClaimView(item: WeightedDeclarationClaim): WeightedDeclarationClaimView {
    return WeightedDeclarationClaimView(
        code = item.code,
        labelText = item.label,
        channel = item.channel,
        visibleText = "${item.visible}",
        driverId = truncate(item.driverId, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDeclarationClaimViews(items: MutableList<WeightedDeclarationClaim>): MutableList<WeightedDeclarationClaimView> {
    val views = mutableListOf<WeightedDeclarationClaimView>()
    for (item in items) {
        views.add(toWeightedDeclarationClaimView(item))
    }
    return views
}

fun sumWeightedDeclarationClaimView(view: WeightedDeclarationClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
