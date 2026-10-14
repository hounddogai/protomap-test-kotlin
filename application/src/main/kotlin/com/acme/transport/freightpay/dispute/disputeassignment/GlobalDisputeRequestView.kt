package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dispute request. */
data class GlobalDisputeRequestView(
    var code: String,
    var nameText: String,
    var minimum: String,
    var channelText: String,
    var rate: String,
    var ratioText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toGlobalDisputeRequestView(item: GlobalDisputeRequest): GlobalDisputeRequestView {
    return GlobalDisputeRequestView(
        code = item.code,
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        channelText = truncate(item.channel, 64),
        rate = "%.2f".format(item.rate),
        ratioText = "%.2f".format(item.ratio),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDisputeRequestViews(items: MutableList<GlobalDisputeRequest>): MutableList<GlobalDisputeRequestView> {
    val views = mutableListOf<GlobalDisputeRequestView>()
    for (item in items) {
        views.add(toGlobalDisputeRequestView(item))
    }
    return views
}

fun sumGlobalDisputeRequestView(view: GlobalDisputeRequestView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedMinimum + parsedRate + parsedRatio
}
