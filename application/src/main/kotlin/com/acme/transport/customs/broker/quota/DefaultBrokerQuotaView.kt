package com.acme.transport.customs.broker.quota

/** How screens and exports show a default broker quota. */
data class DefaultBrokerQuotaView(
    var code: String,
    var nameText: String,
    var amountText: String,
    var summary: String,
    var currency: String,
    var carrierText: String,
    var channelText: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toDefaultBrokerQuotaView(item: DefaultBrokerQuota): DefaultBrokerQuotaView {
    return DefaultBrokerQuotaView(
        code = item.code,
        nameText = item.name,
        amountText = "%.2f".format(item.amount),
        summary = item.summary,
        currency = item.currency,
        carrierText = item.carrier,
        channelText = item.channel,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultBrokerQuotaViews(items: MutableList<DefaultBrokerQuota>): MutableList<DefaultBrokerQuotaView> {
    val views = mutableListOf<DefaultBrokerQuotaView>()
    for (item in items) {
        views.add(toDefaultBrokerQuotaView(item))
    }
    return views
}

fun sumDefaultBrokerQuotaView(view: DefaultBrokerQuotaView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedAmount + parsedMinimum
}
