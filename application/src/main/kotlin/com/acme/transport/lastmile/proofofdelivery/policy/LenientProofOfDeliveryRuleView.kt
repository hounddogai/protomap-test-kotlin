package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient proof of delivery rule. */
data class LenientProofOfDeliveryRuleView(
    var code: String,
    var label: String,
    var limitText: String,
    var timezone: String,
    var archived: String,
    var priorityText: String,
    var description: String,
    var discountText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toLenientProofOfDeliveryRuleView(item: LenientProofOfDeliveryRule): LenientProofOfDeliveryRuleView {
    return LenientProofOfDeliveryRuleView(
        code = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        timezone = truncate(item.timezone, 80),
        archived = "${item.archived}",
        priorityText = item.priority.toString(),
        description = item.description,
        discountText = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientProofOfDeliveryRuleViews(items: MutableList<LenientProofOfDeliveryRule>): MutableList<LenientProofOfDeliveryRuleView> {
    val views = mutableListOf<LenientProofOfDeliveryRuleView>()
    for (item in items) {
        views.add(toLenientProofOfDeliveryRuleView(item))
    }
    return views
}

fun sumLenientProofOfDeliveryRuleView(view: LenientProofOfDeliveryRuleView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedLimit.toDouble() + parsedPriority.toDouble() + parsedDiscount
}
