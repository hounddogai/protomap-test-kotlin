package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration allocation. */
data class DeclarationAllocationView(
    var code: String,
    var name: String,
    var marginText: String,
    var graceDays: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toDeclarationAllocationView(item: DeclarationAllocation): DeclarationAllocationView {
    return DeclarationAllocationView(
        code = item.code,
        name = truncate(item.name, 120),
        marginText = "%.2f".format(item.margin),
        graceDays = item.graceDays.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationAllocationViews(items: MutableList<DeclarationAllocation>): MutableList<DeclarationAllocationView> {
    val views = mutableListOf<DeclarationAllocationView>()
    for (item in items) {
        views.add(toDeclarationAllocationView(item))
    }
    return views
}

fun sumDeclarationAllocationView(view: DeclarationAllocationView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedMargin + parsedGraceDays.toDouble() + parsedDurationMinutes.toDouble()
}
