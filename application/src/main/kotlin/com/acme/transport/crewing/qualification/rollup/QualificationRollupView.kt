package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification rollup. */
data class QualificationRollupView(
    var code: String,
    var labelText: String,
    var dock: String,
    var windowDays: String,
    var timezone: String,
    var score: String,
    var statusLabel: String,
)

fun toQualificationRollupView(item: QualificationRollup): QualificationRollupView {
    return QualificationRollupView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 64),
        dock = truncate(item.dock, 40),
        windowDays = item.windowDays.toString(),
        timezone = item.timezone,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationRollupViews(items: MutableList<QualificationRollup>): MutableList<QualificationRollupView> {
    val views = mutableListOf<QualificationRollupView>()
    for (item in items) {
        views.add(toQualificationRollupView(item))
    }
    return views
}

fun sumQualificationRollupView(view: QualificationRollupView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.score.toDouble()
    return parsedWindowDays.toDouble() + parsedScore
}
