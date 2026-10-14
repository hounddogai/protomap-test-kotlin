package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration entry. */
data class DeclarationEntryView(
    var codeText: String,
    var title: String,
    var phone: String,
    var cost: String,
    var notes: String,
    var trailerId: String,
    var limit: String,
    var statusLabel: String,
)

fun toDeclarationEntryView(item: DeclarationEntry): DeclarationEntryView {
    return DeclarationEntryView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 40),
        phone = truncate(item.phone, 64),
        cost = "%.2f".format(item.cost),
        notes = item.notes,
        trailerId = item.trailerId,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationEntryViews(items: MutableList<DeclarationEntry>): MutableList<DeclarationEntryView> {
    val views = mutableListOf<DeclarationEntryView>()
    for (item in items) {
        views.add(toDeclarationEntryView(item))
    }
    return views
}

fun sumDeclarationEntryView(view: DeclarationEntryView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedCost + parsedLimit.toDouble()
}
