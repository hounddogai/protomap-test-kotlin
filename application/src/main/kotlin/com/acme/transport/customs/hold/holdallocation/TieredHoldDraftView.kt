package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered hold draft. */
data class TieredHoldDraftView(
    var code: String,
    var label: String,
    var loadKgText: String,
    var discount: String,
    var externalRefText: String,
    var region: String,
    var approved: String,
    var statusLabel: String,
)

fun toTieredHoldDraftView(item: TieredHoldDraft): TieredHoldDraftView {
    return TieredHoldDraftView(
        code = item.code,
        label = truncate(item.label, 120),
        loadKgText = "%.2f".format(item.loadKg),
        discount = "%.2f".format(item.discount),
        externalRefText = truncate(item.externalRef, 80),
        region = item.region,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredHoldDraftViews(items: MutableList<TieredHoldDraft>): MutableList<TieredHoldDraftView> {
    val views = mutableListOf<TieredHoldDraftView>()
    for (item in items) {
        views.add(toTieredHoldDraftView(item))
    }
    return views
}

fun sumTieredHoldDraftView(view: TieredHoldDraftView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedLoadKg + parsedDiscount
}
