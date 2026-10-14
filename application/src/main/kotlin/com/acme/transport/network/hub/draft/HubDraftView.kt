package com.acme.transport.network.hub.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub draft. */
data class HubDraftView(
    var codeText: String,
    var name: String,
    var externalRefText: String,
    var discount: String,
    var quantity: String,
    var statusLabel: String,
)

fun toHubDraftView(item: HubDraft): HubDraftView {
    return HubDraftView(
        codeText = item.code,
        name = item.name,
        externalRefText = truncate(item.externalRef, 40),
        discount = "%.2f".format(item.discount),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubDraftViews(items: MutableList<HubDraft>): MutableList<HubDraftView> {
    val views = mutableListOf<HubDraftView>()
    for (item in items) {
        views.add(toHubDraftView(item))
    }
    return views
}

fun sumHubDraftView(view: HubDraftView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedDiscount + parsedQuantity.toDouble()
}
