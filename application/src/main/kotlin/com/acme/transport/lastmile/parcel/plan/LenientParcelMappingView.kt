package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient parcel mapping. */
data class LenientParcelMappingView(
    var code: String,
    var nameText: String,
    var description: String,
    var amount: String,
    var weightText: String,
    var loadKg: String,
    var title: String,
    var currencyText: String,
    var checksum: String,
    var category: String,
    var statusLabel: String,
)

fun toLenientParcelMappingView(item: LenientParcelMapping): LenientParcelMappingView {
    return LenientParcelMappingView(
        code = truncate(item.code, 40),
        nameText = item.name,
        description = item.description,
        amount = "%.2f".format(item.amount),
        weightText = "%.2f".format(item.weight),
        loadKg = "%.2f".format(item.loadKg),
        title = item.title,
        currencyText = item.currency,
        checksum = truncate(item.checksum, 40),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientParcelMappingViews(items: MutableList<LenientParcelMapping>): MutableList<LenientParcelMappingView> {
    val views = mutableListOf<LenientParcelMappingView>()
    for (item in items) {
        views.add(toLenientParcelMappingView(item))
    }
    return views
}

fun sumLenientParcelMappingView(view: LenientParcelMappingView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedAmount + parsedWeight + parsedLoadKg
}
