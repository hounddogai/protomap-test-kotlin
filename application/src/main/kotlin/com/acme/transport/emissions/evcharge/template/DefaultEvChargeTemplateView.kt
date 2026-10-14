package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default ev charge template. */
data class DefaultEvChargeTemplateView(
    var code: String,
    var title: String,
    var score: String,
    var notes: String,
    var region: String,
    var segment: String,
    var dock: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toDefaultEvChargeTemplateView(item: DefaultEvChargeTemplate): DefaultEvChargeTemplateView {
    return DefaultEvChargeTemplateView(
        code = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        notes = item.notes,
        region = truncate(item.region, 64),
        segment = item.segment,
        dock = item.dock,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultEvChargeTemplateViews(items: MutableList<DefaultEvChargeTemplate>): MutableList<DefaultEvChargeTemplateView> {
    val views = mutableListOf<DefaultEvChargeTemplateView>()
    for (item in items) {
        views.add(toDefaultEvChargeTemplateView(item))
    }
    return views
}

fun sumDefaultEvChargeTemplateView(view: DefaultEvChargeTemplateView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
