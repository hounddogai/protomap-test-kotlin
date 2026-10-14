package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default proof of delivery bundle. */
data class DefaultProofOfDeliveryBundleView(
    var code: String,
    var name: String,
    var label: String,
    var activeText: String,
    var amount: String,
    var summary: String,
    var archived: String,
    var weight: String,
    var statusLabel: String,
)

fun toDefaultProofOfDeliveryBundleView(item: DefaultProofOfDeliveryBundle): DefaultProofOfDeliveryBundleView {
    return DefaultProofOfDeliveryBundleView(
        code = item.code,
        name = truncate(item.name, 120),
        label = truncate(item.label, 64),
        activeText = "${item.active}",
        amount = "%.2f".format(item.amount),
        summary = item.summary,
        archived = "${item.archived}",
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultProofOfDeliveryBundleViews(items: MutableList<DefaultProofOfDeliveryBundle>): MutableList<DefaultProofOfDeliveryBundleView> {
    val views = mutableListOf<DefaultProofOfDeliveryBundleView>()
    for (item in items) {
        views.add(toDefaultProofOfDeliveryBundleView(item))
    }
    return views
}

fun sumDefaultProofOfDeliveryBundleView(view: DefaultProofOfDeliveryBundleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedAmount + parsedWeight
}
