package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback duty period override. */
data class FallbackDutyPeriodOverrideView(
    var codeText: String,
    var title: String,
    var dock: String,
    var ceiling: String,
    var etaMinutes: String,
    var summary: String,
    var score: String,
    var statusLabel: String,
)

fun toFallbackDutyPeriodOverrideView(item: FallbackDutyPeriodOverride): FallbackDutyPeriodOverrideView {
    return FallbackDutyPeriodOverrideView(
        codeText = item.code,
        title = truncate(item.title, 64),
        dock = truncate(item.dock, 80),
        ceiling = "%.2f".format(item.ceiling),
        etaMinutes = item.etaMinutes.toString(),
        summary = truncate(item.summary, 64),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDutyPeriodOverrideViews(items: MutableList<FallbackDutyPeriodOverride>): MutableList<FallbackDutyPeriodOverrideView> {
    val views = mutableListOf<FallbackDutyPeriodOverrideView>()
    for (item in items) {
        views.add(toFallbackDutyPeriodOverrideView(item))
    }
    return views
}

fun sumFallbackDutyPeriodOverrideView(view: FallbackDutyPeriodOverrideView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedScore = view.score.toDouble()
    return parsedCeiling + parsedEtaMinutes.toDouble() + parsedScore
}
