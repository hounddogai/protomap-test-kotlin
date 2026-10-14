package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lane plan draft. */
data class ScheduledLanePlanDraftView(
    var code: String,
    var titleText: String,
    var sequenceText: String,
    var carrierText: String,
    var retriesText: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toScheduledLanePlanDraftView(item: ScheduledLanePlanDraft): ScheduledLanePlanDraftView {
    return ScheduledLanePlanDraftView(
        code = item.code,
        titleText = truncate(item.title, 120),
        sequenceText = item.sequence.toString(),
        carrierText = truncate(item.carrier, 80),
        retriesText = item.retries.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanDraftViews(items: MutableList<ScheduledLanePlanDraft>): MutableList<ScheduledLanePlanDraftView> {
    val views = mutableListOf<ScheduledLanePlanDraftView>()
    for (item in items) {
        views.add(toScheduledLanePlanDraftView(item))
    }
    return views
}

fun sumScheduledLanePlanDraftView(view: ScheduledLanePlanDraftView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedSequence.toDouble() + parsedRetries.toDouble() + parsedOdometer.toDouble()
}
