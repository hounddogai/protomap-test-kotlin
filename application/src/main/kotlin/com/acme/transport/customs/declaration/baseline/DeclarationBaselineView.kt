package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration baseline. */
data class DeclarationBaselineView(
    var code: String,
    var name: String,
    var amountText: String,
    var rate: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toDeclarationBaselineView(item: DeclarationBaseline): DeclarationBaselineView {
    return DeclarationBaselineView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 80),
        amountText = "%.2f".format(item.amount),
        rate = "%.2f".format(item.rate),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationBaselineViews(items: MutableList<DeclarationBaseline>): MutableList<DeclarationBaselineView> {
    val views = mutableListOf<DeclarationBaselineView>()
    for (item in items) {
        views.add(toDeclarationBaselineView(item))
    }
    return views
}

fun sumDeclarationBaselineView(view: DeclarationBaselineView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedAmount + parsedRate
}
