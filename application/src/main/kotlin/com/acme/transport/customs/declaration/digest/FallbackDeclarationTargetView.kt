package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback declaration target. */
data class FallbackDeclarationTargetView(
    var code: String,
    var title: String,
    var windowDays: String,
    var trailerIdText: String,
    var createdAtText: String,
    var regionText: String,
    var discount: String,
    var notesText: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toFallbackDeclarationTargetView(item: FallbackDeclarationTarget): FallbackDeclarationTargetView {
    return FallbackDeclarationTargetView(
        code = item.code,
        title = truncate(item.title, 120),
        windowDays = item.windowDays.toString(),
        trailerIdText = item.trailerId,
        createdAtText = item.createdAt.toString(),
        regionText = truncate(item.region, 64),
        discount = "%.2f".format(item.discount),
        notesText = item.notes,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDeclarationTargetViews(items: MutableList<FallbackDeclarationTarget>): MutableList<FallbackDeclarationTargetView> {
    val views = mutableListOf<FallbackDeclarationTargetView>()
    for (item in items) {
        views.add(toFallbackDeclarationTargetView(item))
    }
    return views
}

fun sumFallbackDeclarationTargetView(view: FallbackDeclarationTargetView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedWindowDays.toDouble() + parsedCreatedAt.toDouble() + parsedDiscount
}
