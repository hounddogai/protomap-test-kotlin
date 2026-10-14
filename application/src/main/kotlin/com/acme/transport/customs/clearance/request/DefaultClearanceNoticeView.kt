package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default clearance notice. */
data class DefaultClearanceNoticeView(
    var code: String,
    var title: String,
    var carrier: String,
    var pallets: String,
    var descriptionText: String,
    var dock: String,
    var amount: String,
    var ceiling: String,
    var category: String,
    var label: String,
    var nameText: String,
    var statusLabel: String,
)

fun toDefaultClearanceNoticeView(item: DefaultClearanceNotice): DefaultClearanceNoticeView {
    return DefaultClearanceNoticeView(
        code = item.code,
        title = item.title,
        carrier = item.carrier,
        pallets = item.pallets.toString(),
        descriptionText = truncate(item.description, 80),
        dock = item.dock,
        amount = "%.2f".format(item.amount),
        ceiling = "%.2f".format(item.ceiling),
        category = truncate(item.category, 40),
        label = item.label,
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultClearanceNoticeViews(items: MutableList<DefaultClearanceNotice>): MutableList<DefaultClearanceNoticeView> {
    val views = mutableListOf<DefaultClearanceNoticeView>()
    for (item in items) {
        views.add(toDefaultClearanceNoticeView(item))
    }
    return views
}

fun sumDefaultClearanceNoticeView(view: DefaultClearanceNoticeView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedPallets.toDouble() + parsedAmount + parsedCeiling
}
