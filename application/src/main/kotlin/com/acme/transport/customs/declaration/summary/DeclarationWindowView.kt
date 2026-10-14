package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration window. */
data class DeclarationWindowView(
    var code: String,
    var name: String,
    var weight: String,
    var checksum: String,
    var labelText: String,
    var routeCode: String,
    var fuelLitersText: String,
    var phone: String,
    var statusLabel: String,
)

fun toDeclarationWindowView(item: DeclarationWindow): DeclarationWindowView {
    return DeclarationWindowView(
        code = item.code,
        name = item.name,
        weight = "%.2f".format(item.weight),
        checksum = truncate(item.checksum, 120),
        labelText = item.label,
        routeCode = item.routeCode,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationWindowViews(items: MutableList<DeclarationWindow>): MutableList<DeclarationWindowView> {
    val views = mutableListOf<DeclarationWindowView>()
    for (item in items) {
        views.add(toDeclarationWindowView(item))
    }
    return views
}

fun sumDeclarationWindowView(view: DeclarationWindowView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedWeight + parsedFuelLiters
}
