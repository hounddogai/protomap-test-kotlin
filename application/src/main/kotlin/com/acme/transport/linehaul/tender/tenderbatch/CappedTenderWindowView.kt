package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped tender window. */
data class CappedTenderWindowView(
    var code: String,
    var name: String,
    var title: String,
    var capacity: String,
    var ceiling: String,
    var quantity: String,
    var phone: String,
    var statusLabel: String,
)

fun toCappedTenderWindowView(item: CappedTenderWindow): CappedTenderWindowView {
    return CappedTenderWindowView(
        code = item.code,
        name = truncate(item.name, 40),
        title = item.title,
        capacity = item.capacity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        phone = truncate(item.phone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTenderWindowViews(items: MutableList<CappedTenderWindow>): MutableList<CappedTenderWindowView> {
    val views = mutableListOf<CappedTenderWindowView>()
    for (item in items) {
        views.add(toCappedTenderWindowView(item))
    }
    return views
}

fun sumCappedTenderWindowView(view: CappedTenderWindowView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedCapacity.toDouble() + parsedCeiling + parsedQuantity.toDouble()
}
