package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling defect exception. */
data class RollingDefectExceptionView(
    var code: String,
    var titleText: String,
    var segment: String,
    var carrier: String,
    var externalRefText: String,
    var shiftCode: String,
    var durationMinutes: String,
    var attempts: String,
    var createdAt: String,
    var score: String,
    var statusLabel: String,
)

fun toRollingDefectExceptionView(item: RollingDefectException): RollingDefectExceptionView {
    return RollingDefectExceptionView(
        code = truncate(item.code, 80),
        titleText = item.title,
        segment = item.segment,
        carrier = item.carrier,
        externalRefText = item.externalRef,
        shiftCode = truncate(item.shiftCode, 80),
        durationMinutes = item.durationMinutes.toString(),
        attempts = item.attempts.toString(),
        createdAt = item.createdAt.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDefectExceptionViews(items: MutableList<RollingDefectException>): MutableList<RollingDefectExceptionView> {
    val views = mutableListOf<RollingDefectExceptionView>()
    for (item in items) {
        views.add(toRollingDefectExceptionView(item))
    }
    return views
}

fun sumRollingDefectExceptionView(view: RollingDefectExceptionView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedAttempts.toDouble() + parsedCreatedAt.toDouble()
}
