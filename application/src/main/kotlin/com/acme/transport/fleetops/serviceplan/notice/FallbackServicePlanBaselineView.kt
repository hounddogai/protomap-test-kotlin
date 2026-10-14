package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan baseline. */
data class FallbackServicePlanBaselineView(
    var codeText: String,
    var name: String,
    var priorityText: String,
    var channel: String,
    var createdAt: String,
    var externalRef: String,
    var sequence: String,
    var windowDays: String,
    var shiftCode: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toFallbackServicePlanBaselineView(item: FallbackServicePlanBaseline): FallbackServicePlanBaselineView {
    return FallbackServicePlanBaselineView(
        codeText = item.code,
        name = truncate(item.name, 120),
        priorityText = item.priority.toString(),
        channel = item.channel,
        createdAt = item.createdAt.toString(),
        externalRef = item.externalRef,
        sequence = item.sequence.toString(),
        windowDays = item.windowDays.toString(),
        shiftCode = item.shiftCode,
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanBaselineViews(items: MutableList<FallbackServicePlanBaseline>): MutableList<FallbackServicePlanBaselineView> {
    val views = mutableListOf<FallbackServicePlanBaselineView>()
    for (item in items) {
        views.add(toFallbackServicePlanBaselineView(item))
    }
    return views
}

fun sumFallbackServicePlanBaselineView(view: FallbackServicePlanBaselineView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedPriority.toDouble() + parsedCreatedAt.toDouble() + parsedSequence.toDouble()
}
