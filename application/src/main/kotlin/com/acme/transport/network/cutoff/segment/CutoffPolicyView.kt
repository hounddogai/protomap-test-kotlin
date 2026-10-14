package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff policy. */
data class CutoffPolicyView(
    var code: String,
    var title: String,
    var pinned: String,
    var currency: String,
    var externalRef: String,
    var phone: String,
    var notes: String,
    var endsAt: String,
    var quantityText: String,
    var locale: String,
    var statusLabel: String,
)

fun toCutoffPolicyView(item: CutoffPolicy): CutoffPolicyView {
    return CutoffPolicyView(
        code = item.code,
        title = item.title,
        pinned = "${item.pinned}",
        currency = truncate(item.currency, 120),
        externalRef = item.externalRef,
        phone = item.phone,
        notes = item.notes,
        endsAt = item.endsAt.toString(),
        quantityText = item.quantity.toString(),
        locale = truncate(item.locale, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffPolicyViews(items: MutableList<CutoffPolicy>): MutableList<CutoffPolicyView> {
    val views = mutableListOf<CutoffPolicyView>()
    for (item in items) {
        views.add(toCutoffPolicyView(item))
    }
    return views
}

fun sumCutoffPolicyView(view: CutoffPolicyView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedEndsAt.toDouble() + parsedQuantity.toDouble()
}
