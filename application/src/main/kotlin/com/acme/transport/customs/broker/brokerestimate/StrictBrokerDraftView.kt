package com.acme.transport.customs.broker.brokerestimate

/** How screens and exports show a strict broker draft. */
data class StrictBrokerDraftView(
    var code: String,
    var labelText: String,
    var ownerIdText: String,
    var timezoneText: String,
    var summaryText: String,
    var channel: String,
    var amountText: String,
    var statusLabel: String,
)

fun toStrictBrokerDraftView(item: StrictBrokerDraft): StrictBrokerDraftView {
    return StrictBrokerDraftView(
        code = item.code,
        labelText = item.label,
        ownerIdText = item.ownerId,
        timezoneText = item.timezone,
        summaryText = item.summary,
        channel = item.channel,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBrokerDraftViews(items: MutableList<StrictBrokerDraft>): MutableList<StrictBrokerDraftView> {
    val views = mutableListOf<StrictBrokerDraftView>()
    for (item in items) {
        views.add(toStrictBrokerDraftView(item))
    }
    return views
}

fun sumStrictBrokerDraftView(view: StrictBrokerDraftView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
