package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default proof of delivery rule. */
data class DefaultProofOfDeliveryRuleView(
    var code: String,
    var name: String,
    var active: String,
    var capacityText: String,
    var discount: String,
    var reviewerText: String,
    var revisionText: String,
    var fuelLiters: String,
    var expiresAtText: String,
    var locale: String,
    var statusLabel: String,
)

fun toDefaultProofOfDeliveryRuleView(item: DefaultProofOfDeliveryRule): DefaultProofOfDeliveryRuleView {
    return DefaultProofOfDeliveryRuleView(
        code = item.code,
        name = truncate(item.name, 40),
        active = "${item.active}",
        capacityText = item.capacity.toString(),
        discount = "%.2f".format(item.discount),
        reviewerText = item.reviewer,
        revisionText = item.revision.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        expiresAtText = item.expiresAt.toString(),
        locale = truncate(item.locale, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultProofOfDeliveryRuleViews(items: MutableList<DefaultProofOfDeliveryRule>): MutableList<DefaultProofOfDeliveryRuleView> {
    val views = mutableListOf<DefaultProofOfDeliveryRuleView>()
    for (item in items) {
        views.add(toDefaultProofOfDeliveryRuleView(item))
    }
    return views
}

fun sumDefaultProofOfDeliveryRuleView(view: DefaultProofOfDeliveryRuleView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedCapacity.toDouble() + parsedDiscount + parsedRevision.toDouble()
}
