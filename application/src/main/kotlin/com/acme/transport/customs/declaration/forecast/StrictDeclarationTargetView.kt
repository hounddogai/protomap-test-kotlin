package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict declaration target. */
data class StrictDeclarationTargetView(
    var code: String,
    var label: String,
    var approved: String,
    var weight: String,
    var countryText: String,
    var name: String,
    var statusLabel: String,
)

fun toStrictDeclarationTargetView(item: StrictDeclarationTarget): StrictDeclarationTargetView {
    return StrictDeclarationTargetView(
        code = item.code,
        label = truncate(item.label, 80),
        approved = "${item.approved}",
        weight = "%.2f".format(item.weight),
        countryText = truncate(item.country, 80),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDeclarationTargetViews(items: MutableList<StrictDeclarationTarget>): MutableList<StrictDeclarationTargetView> {
    val views = mutableListOf<StrictDeclarationTargetView>()
    for (item in items) {
        views.add(toStrictDeclarationTargetView(item))
    }
    return views
}

fun sumStrictDeclarationTargetView(view: StrictDeclarationTargetView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
