package com.acme.transport.lastmile.redelivery.redeliveryquota

/** How screens and exports show a primary redelivery policy. */
data class PrimaryRedeliveryPolicyView(
    var code: String,
    var name: String,
    var archivedText: String,
    var carrierText: String,
    var amount: String,
    var ceilingText: String,
    var label: String,
    var rateText: String,
    var title: String,
    var floorValue: String,
    var country: String,
    var statusLabel: String,
)

fun toPrimaryRedeliveryPolicyView(item: PrimaryRedeliveryPolicy): PrimaryRedeliveryPolicyView {
    return PrimaryRedeliveryPolicyView(
        code = item.code,
        name = item.name,
        archivedText = "${item.archived}",
        carrierText = item.carrier,
        amount = "%.2f".format(item.amount),
        ceilingText = "%.2f".format(item.ceiling),
        label = item.label,
        rateText = "%.2f".format(item.rate),
        title = item.title,
        floorValue = "%.2f".format(item.floorValue),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRedeliveryPolicyViews(items: MutableList<PrimaryRedeliveryPolicy>): MutableList<PrimaryRedeliveryPolicyView> {
    val views = mutableListOf<PrimaryRedeliveryPolicyView>()
    for (item in items) {
        views.add(toPrimaryRedeliveryPolicyView(item))
    }
    return views
}

fun sumPrimaryRedeliveryPolicyView(view: PrimaryRedeliveryPolicyView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedAmount + parsedCeiling + parsedRate
}
