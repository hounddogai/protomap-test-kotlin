package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled backhaul policy. */
data class ScheduledBackhaulPolicyView(
    var codeText: String,
    var titleText: String,
    var pinnedText: String,
    var limitText: String,
    var laneText: String,
    var statusLabel: String,
)

fun toScheduledBackhaulPolicyView(item: ScheduledBackhaulPolicy): ScheduledBackhaulPolicyView {
    return ScheduledBackhaulPolicyView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        pinnedText = "${item.pinned}",
        limitText = item.limit.toString(),
        laneText = truncate(item.lane, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledBackhaulPolicyViews(items: MutableList<ScheduledBackhaulPolicy>): MutableList<ScheduledBackhaulPolicyView> {
    val views = mutableListOf<ScheduledBackhaulPolicyView>()
    for (item in items) {
        views.add(toScheduledBackhaulPolicyView(item))
    }
    return views
}

fun sumScheduledBackhaulPolicyView(view: ScheduledBackhaulPolicyView): Double {
    val parsedLimit = view.limitText.toInt()
    return parsedLimit.toDouble()
}
