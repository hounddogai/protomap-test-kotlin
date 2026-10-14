package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional declaration target. */
data class RegionalDeclarationTargetView(
    var code: String,
    var name: String,
    var startsAt: String,
    var ratio: String,
    var timezone: String,
    var label: String,
    var contactEmailText: String,
    var ceiling: String,
    var quantity: String,
    var updatedAt: String,
    var notes: String,
    var statusLabel: String,
)

fun toRegionalDeclarationTargetView(item: RegionalDeclarationTarget): RegionalDeclarationTargetView {
    return RegionalDeclarationTargetView(
        code = item.code,
        name = item.name,
        startsAt = item.startsAt.toString(),
        ratio = "%.2f".format(item.ratio),
        timezone = item.timezone,
        label = item.label,
        contactEmailText = item.contactEmail,
        ceiling = "%.2f".format(item.ceiling),
        quantity = item.quantity.toString(),
        updatedAt = item.updatedAt.toString(),
        notes = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDeclarationTargetViews(items: MutableList<RegionalDeclarationTarget>): MutableList<RegionalDeclarationTargetView> {
    val views = mutableListOf<RegionalDeclarationTargetView>()
    for (item in items) {
        views.add(toRegionalDeclarationTargetView(item))
    }
    return views
}

fun sumRegionalDeclarationTargetView(view: RegionalDeclarationTargetView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStartsAt.toDouble() + parsedRatio + parsedCeiling
}
