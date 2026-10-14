package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal declaration target. */
data class SeasonalDeclarationTargetView(
    var code: String,
    var titleText: String,
    var externalRef: String,
    var expiresAt: String,
    var description: String,
    var shiftCode: String,
    var maximum: String,
    var statusLabel: String,
)

fun toSeasonalDeclarationTargetView(item: SeasonalDeclarationTarget): SeasonalDeclarationTargetView {
    return SeasonalDeclarationTargetView(
        code = truncate(item.code, 40),
        titleText = item.title,
        externalRef = item.externalRef,
        expiresAt = item.expiresAt.toString(),
        description = item.description,
        shiftCode = item.shiftCode,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDeclarationTargetViews(items: MutableList<SeasonalDeclarationTarget>): MutableList<SeasonalDeclarationTargetView> {
    val views = mutableListOf<SeasonalDeclarationTargetView>()
    for (item in items) {
        views.add(toSeasonalDeclarationTargetView(item))
    }
    return views
}

fun sumSeasonalDeclarationTargetView(view: SeasonalDeclarationTargetView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedExpiresAt.toDouble() + parsedMaximum
}
