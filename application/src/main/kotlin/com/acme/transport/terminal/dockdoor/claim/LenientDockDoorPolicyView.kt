package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dock door policy. */
data class LenientDockDoorPolicyView(
    var code: String,
    var title: String,
    var etaMinutes: String,
    var summary: String,
    var carrier: String,
    var priorityText: String,
    var phone: String,
    var limit: String,
    var windowDays: String,
    var timezone: String,
    var threshold: String,
    var statusLabel: String,
)

fun toLenientDockDoorPolicyView(item: LenientDockDoorPolicy): LenientDockDoorPolicyView {
    return LenientDockDoorPolicyView(
        code = truncate(item.code, 80),
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        summary = truncate(item.summary, 40),
        carrier = item.carrier,
        priorityText = item.priority.toString(),
        phone = item.phone,
        limit = item.limit.toString(),
        windowDays = item.windowDays.toString(),
        timezone = item.timezone,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDockDoorPolicyViews(items: MutableList<LenientDockDoorPolicy>): MutableList<LenientDockDoorPolicyView> {
    val views = mutableListOf<LenientDockDoorPolicyView>()
    for (item in items) {
        views.add(toLenientDockDoorPolicyView(item))
    }
    return views
}

fun sumLenientDockDoorPolicyView(view: LenientDockDoorPolicyView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedEtaMinutes.toDouble() + parsedPriority.toDouble() + parsedLimit.toDouble()
}
