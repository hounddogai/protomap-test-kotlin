package com.acme.transport.freightpay.dispute.request

/** How screens and exports show a legacy dispute profile. */
data class LegacyDisputeProfileView(
    var code: String,
    var label: String,
    var approved: String,
    var channel: String,
    var phoneText: String,
    var cost: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toLegacyDisputeProfileView(item: LegacyDisputeProfile): LegacyDisputeProfileView {
    return LegacyDisputeProfileView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        channel = item.channel,
        phoneText = item.phone,
        cost = "%.2f".format(item.cost),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDisputeProfileViews(items: MutableList<LegacyDisputeProfile>): MutableList<LegacyDisputeProfileView> {
    val views = mutableListOf<LegacyDisputeProfileView>()
    for (item in items) {
        views.add(toLegacyDisputeProfileView(item))
    }
    return views
}

fun sumLegacyDisputeProfileView(view: LegacyDisputeProfileView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCost + parsedThreshold
}
