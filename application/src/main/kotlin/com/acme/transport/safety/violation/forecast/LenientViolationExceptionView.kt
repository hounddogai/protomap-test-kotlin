package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient violation exception. */
data class LenientViolationExceptionView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var archived: String,
    var ratio: String,
    var floorValue: String,
    var timezoneText: String,
    var stopCount: String,
    var threshold: String,
    var statusLabel: String,
)

fun toLenientViolationExceptionView(item: LenientViolationException): LenientViolationExceptionView {
    return LenientViolationExceptionView(
        code = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        archived = "${item.archived}",
        ratio = "%.2f".format(item.ratio),
        floorValue = "%.2f".format(item.floorValue),
        timezoneText = truncate(item.timezone, 64),
        stopCount = item.stopCount.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientViolationExceptionViews(items: MutableList<LenientViolationException>): MutableList<LenientViolationExceptionView> {
    val views = mutableListOf<LenientViolationExceptionView>()
    for (item in items) {
        views.add(toLenientViolationExceptionView(item))
    }
    return views
}

fun sumLenientViolationExceptionView(view: LenientViolationExceptionView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedAttempts.toDouble() + parsedRatio + parsedFloorValue
}
