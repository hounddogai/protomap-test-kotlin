package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor notice. */
data class EmissionFactorNoticeView(
    var codeText: String,
    var label: String,
    var updatedAtText: String,
    var windowDays: String,
    var score: String,
    var statusLabel: String,
)

fun toEmissionFactorNoticeView(item: EmissionFactorNotice): EmissionFactorNoticeView {
    return EmissionFactorNoticeView(
        codeText = item.code,
        label = truncate(item.label, 40),
        updatedAtText = item.updatedAt.toString(),
        windowDays = item.windowDays.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorNoticeViews(items: MutableList<EmissionFactorNotice>): MutableList<EmissionFactorNoticeView> {
    val views = mutableListOf<EmissionFactorNoticeView>()
    for (item in items) {
        views.add(toEmissionFactorNoticeView(item))
    }
    return views
}

fun sumEmissionFactorNoticeView(view: EmissionFactorNoticeView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.score.toDouble()
    return parsedUpdatedAt.toDouble() + parsedWindowDays.toDouble() + parsedScore
}
