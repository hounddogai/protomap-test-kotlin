package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient tender group. */
data class LenientTenderGroupView(
    var codeText: String,
    var title: String,
    var country: String,
    var quantityText: String,
    var ownerIdText: String,
    var maximumText: String,
    var routeCode: String,
    var name: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toLenientTenderGroupView(item: LenientTenderGroup): LenientTenderGroupView {
    return LenientTenderGroupView(
        codeText = truncate(item.code, 64),
        title = item.title,
        country = truncate(item.country, 64),
        quantityText = item.quantity.toString(),
        ownerIdText = truncate(item.ownerId, 40),
        maximumText = "%.2f".format(item.maximum),
        routeCode = truncate(item.routeCode, 80),
        name = item.name,
        reviewerText = truncate(item.reviewer, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTenderGroupViews(items: MutableList<LenientTenderGroup>): MutableList<LenientTenderGroupView> {
    val views = mutableListOf<LenientTenderGroupView>()
    for (item in items) {
        views.add(toLenientTenderGroupView(item))
    }
    return views
}

fun sumLenientTenderGroupView(view: LenientTenderGroupView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedQuantity.toDouble() + parsedMaximum
}
