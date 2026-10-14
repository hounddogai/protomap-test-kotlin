package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched tariff digest. */
data class BatchedTariffDigestView(
    var code: String,
    var titleText: String,
    var carrier: String,
    var locale: String,
    var depotText: String,
    var etaMinutes: String,
    var name: String,
    var currency: String,
    var visible: String,
    var cost: String,
    var statusLabel: String,
)

fun toBatchedTariffDigestView(item: BatchedTariffDigest): BatchedTariffDigestView {
    return BatchedTariffDigestView(
        code = truncate(item.code, 64),
        titleText = item.title,
        carrier = truncate(item.carrier, 64),
        locale = truncate(item.locale, 64),
        depotText = item.depot,
        etaMinutes = item.etaMinutes.toString(),
        name = truncate(item.name, 80),
        currency = item.currency,
        visible = "${item.visible}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTariffDigestViews(items: MutableList<BatchedTariffDigest>): MutableList<BatchedTariffDigestView> {
    val views = mutableListOf<BatchedTariffDigestView>()
    for (item in items) {
        views.add(toBatchedTariffDigestView(item))
    }
    return views
}

fun sumBatchedTariffDigestView(view: BatchedTariffDigestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedEtaMinutes.toDouble() + parsedCost
}
