package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability policy. */
data class AvailabilityPolicyView(
    var code: String,
    var titleText: String,
    var segmentText: String,
    var region: String,
    var sequenceText: String,
    var marginText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toAvailabilityPolicyView(item: AvailabilityPolicy): AvailabilityPolicyView {
    return AvailabilityPolicyView(
        code = item.code,
        titleText = item.title,
        segmentText = truncate(item.segment, 40),
        region = item.region,
        sequenceText = item.sequence.toString(),
        marginText = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityPolicyViews(items: MutableList<AvailabilityPolicy>): MutableList<AvailabilityPolicyView> {
    val views = mutableListOf<AvailabilityPolicyView>()
    for (item in items) {
        views.add(toAvailabilityPolicyView(item))
    }
    return views
}

fun sumAvailabilityPolicyView(view: AvailabilityPolicyView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedSequence.toDouble() + parsedMargin
}
