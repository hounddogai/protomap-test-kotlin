package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling policy. */
data class IdlingPolicyView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var thresholdText: String,
    var unitText: String,
    var contactEmail: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toIdlingPolicyView(item: IdlingPolicy): IdlingPolicyView {
    return IdlingPolicyView(
        codeText = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        thresholdText = "%.2f".format(item.threshold),
        unitText = truncate(item.unit, 40),
        contactEmail = item.contactEmail,
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingPolicyViews(items: MutableList<IdlingPolicy>): MutableList<IdlingPolicyView> {
    val views = mutableListOf<IdlingPolicyView>()
    for (item in items) {
        views.add(toIdlingPolicyView(item))
    }
    return views
}

fun sumIdlingPolicyView(view: IdlingPolicyView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCeiling + parsedThreshold
}
