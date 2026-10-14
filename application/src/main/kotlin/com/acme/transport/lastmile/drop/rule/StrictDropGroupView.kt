package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict drop group. */
data class StrictDropGroupView(
    var code: String,
    var title: String,
    var quantity: String,
    var description: String,
    var discount: String,
    var dockText: String,
    var statusLabel: String,
)

fun toStrictDropGroupView(item: StrictDropGroup): StrictDropGroupView {
    return StrictDropGroupView(
        code = truncate(item.code, 40),
        title = item.title,
        quantity = item.quantity.toString(),
        description = item.description,
        discount = "%.2f".format(item.discount),
        dockText = truncate(item.dock, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDropGroupViews(items: MutableList<StrictDropGroup>): MutableList<StrictDropGroupView> {
    val views = mutableListOf<StrictDropGroupView>()
    for (item in items) {
        views.add(toStrictDropGroupView(item))
    }
    return views
}

fun sumStrictDropGroupView(view: StrictDropGroupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedQuantity.toDouble() + parsedDiscount
}
