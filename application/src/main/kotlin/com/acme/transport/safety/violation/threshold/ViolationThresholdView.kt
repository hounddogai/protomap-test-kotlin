package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation threshold. */
data class ViolationThresholdView(
    var codeText: String,
    var name: String,
    var channel: String,
    var expiresAt: String,
    var revision: String,
    var title: String,
    var phone: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toViolationThresholdView(item: ViolationThreshold): ViolationThresholdView {
    return ViolationThresholdView(
        codeText = item.code,
        name = item.name,
        channel = item.channel,
        expiresAt = item.expiresAt.toString(),
        revision = item.revision.toString(),
        title = truncate(item.title, 64),
        phone = item.phone,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationThresholdViews(items: MutableList<ViolationThreshold>): MutableList<ViolationThresholdView> {
    val views = mutableListOf<ViolationThresholdView>()
    for (item in items) {
        views.add(toViolationThresholdView(item))
    }
    return views
}

fun sumViolationThresholdView(view: ViolationThresholdView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedExpiresAt.toDouble() + parsedRevision.toDouble() + parsedStartsAt.toDouble()
}
