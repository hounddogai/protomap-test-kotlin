package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tariff request. */
data class LenientTariffRequestView(
    var codeText: String,
    var name: String,
    var threshold: String,
    var updatedAt: String,
    var ceiling: String,
    var createdAtText: String,
    var version: String,
    var statusLabel: String,
)

fun toLenientTariffRequestView(item: LenientTariffRequest): LenientTariffRequestView {
    return LenientTariffRequestView(
        codeText = truncate(item.code, 40),
        name = item.name,
        threshold = "%.2f".format(item.threshold),
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        createdAtText = item.createdAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTariffRequestViews(items: MutableList<LenientTariffRequest>): MutableList<LenientTariffRequestView> {
    val views = mutableListOf<LenientTariffRequestView>()
    for (item in items) {
        views.add(toLenientTariffRequestView(item))
    }
    return views
}

fun sumLenientTariffRequestView(view: LenientTariffRequestView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedThreshold + parsedUpdatedAt.toDouble() + parsedCeiling
}
