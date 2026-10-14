package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered declaration score. */
data class TieredDeclarationScoreView(
    var code: String,
    var labelText: String,
    var phone: String,
    var approvedText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toTieredDeclarationScoreView(item: TieredDeclarationScore): TieredDeclarationScoreView {
    return TieredDeclarationScoreView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 64),
        phone = truncate(item.phone, 80),
        approvedText = "${item.approved}",
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDeclarationScoreViews(items: MutableList<TieredDeclarationScore>): MutableList<TieredDeclarationScoreView> {
    val views = mutableListOf<TieredDeclarationScoreView>()
    for (item in items) {
        views.add(toTieredDeclarationScoreView(item))
    }
    return views
}

fun sumTieredDeclarationScoreView(view: TieredDeclarationScoreView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}
