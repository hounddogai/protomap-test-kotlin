package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation quota. */
data class ViolationQuotaView(
    var codeText: String,
    var title: String,
    var ratio: String,
    var limit: String,
    var priorityText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toViolationQuotaView(item: ViolationQuota): ViolationQuotaView {
    return ViolationQuotaView(
        codeText = truncate(item.code, 80),
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        limit = item.limit.toString(),
        priorityText = item.priority.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationQuotaViews(items: MutableList<ViolationQuota>): MutableList<ViolationQuotaView> {
    val views = mutableListOf<ViolationQuotaView>()
    for (item in items) {
        views.add(toViolationQuotaView(item))
    }
    return views
}

fun sumViolationQuotaView(view: ViolationQuotaView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedRatio + parsedLimit.toDouble() + parsedPriority.toDouble()
}
