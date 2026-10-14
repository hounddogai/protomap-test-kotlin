package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff request. */
data class TariffRequestView(
    var codeText: String,
    var title: String,
    var discount: String,
    var phoneText: String,
    var ceiling: String,
    var visibleText: String,
    var budgetText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toTariffRequestView(item: TariffRequest): TariffRequestView {
    return TariffRequestView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 80),
        discount = "%.2f".format(item.discount),
        phoneText = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        visibleText = "${item.visible}",
        budgetText = "%.2f".format(item.budget),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffRequestViews(items: MutableList<TariffRequest>): MutableList<TariffRequestView> {
    val views = mutableListOf<TariffRequestView>()
    for (item in items) {
        views.add(toTariffRequestView(item))
    }
    return views
}

fun sumTariffRequestView(view: TariffRequestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedDiscount + parsedCeiling + parsedBudget
}
