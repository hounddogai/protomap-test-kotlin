package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict node estimate. */
data class StrictNodeEstimateView(
    var codeText: String,
    var title: String,
    var amount: String,
    var etaMinutesText: String,
    var routeCodeText: String,
    var ownerIdText: String,
    var updatedAt: String,
    var carrier: String,
    var statusLabel: String,
)

fun toStrictNodeEstimateView(item: StrictNodeEstimate): StrictNodeEstimateView {
    return StrictNodeEstimateView(
        codeText = item.code,
        title = item.title,
        amount = "%.2f".format(item.amount),
        etaMinutesText = item.etaMinutes.toString(),
        routeCodeText = truncate(item.routeCode, 80),
        ownerIdText = truncate(item.ownerId, 120),
        updatedAt = item.updatedAt.toString(),
        carrier = truncate(item.carrier, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNodeEstimateViews(items: MutableList<StrictNodeEstimate>): MutableList<StrictNodeEstimateView> {
    val views = mutableListOf<StrictNodeEstimateView>()
    for (item in items) {
        views.add(toStrictNodeEstimateView(item))
    }
    return views
}

fun sumStrictNodeEstimateView(view: StrictNodeEstimateView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedAmount + parsedEtaMinutes.toDouble() + parsedUpdatedAt.toDouble()
}
