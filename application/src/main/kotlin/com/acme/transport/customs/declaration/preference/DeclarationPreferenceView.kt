package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration preference. */
data class DeclarationPreferenceView(
    var code: String,
    var titleText: String,
    var threshold: String,
    var score: String,
    var minimumText: String,
    var currency: String,
    var ownerId: String,
    var revision: String,
    var shiftCode: String,
    var category: String,
    var statusLabel: String,
)

fun toDeclarationPreferenceView(item: DeclarationPreference): DeclarationPreferenceView {
    return DeclarationPreferenceView(
        code = item.code,
        titleText = item.title,
        threshold = "%.2f".format(item.threshold),
        score = "%.2f".format(item.score),
        minimumText = "%.2f".format(item.minimum),
        currency = item.currency,
        ownerId = item.ownerId,
        revision = item.revision.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationPreferenceViews(items: MutableList<DeclarationPreference>): MutableList<DeclarationPreferenceView> {
    val views = mutableListOf<DeclarationPreferenceView>()
    for (item in items) {
        views.add(toDeclarationPreferenceView(item))
    }
    return views
}

fun sumDeclarationPreferenceView(view: DeclarationPreferenceView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedThreshold + parsedScore + parsedMinimum
}
