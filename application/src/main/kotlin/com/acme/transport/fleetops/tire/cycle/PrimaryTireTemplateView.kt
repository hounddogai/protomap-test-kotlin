package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tire template. */
data class PrimaryTireTemplateView(
    var codeText: String,
    var labelText: String,
    var updatedAt: String,
    var title: String,
    var scoreText: String,
    var categoryText: String,
    var locale: String,
    var statusLabel: String,
)

fun toPrimaryTireTemplateView(item: PrimaryTireTemplate): PrimaryTireTemplateView {
    return PrimaryTireTemplateView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        updatedAt = item.updatedAt.toString(),
        title = item.title,
        scoreText = "%.2f".format(item.score),
        categoryText = item.category,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTireTemplateViews(items: MutableList<PrimaryTireTemplate>): MutableList<PrimaryTireTemplateView> {
    val views = mutableListOf<PrimaryTireTemplateView>()
    for (item in items) {
        views.add(toPrimaryTireTemplateView(item))
    }
    return views
}

fun sumPrimaryTireTemplateView(view: PrimaryTireTemplateView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedScore
}
