package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict availability notice. */
data class StrictAvailabilityNoticeView(
    var codeText: String,
    var titleText: String,
    var channel: String,
    var ceiling: String,
    var sequenceText: String,
    var category: String,
    var statusLabel: String,
)

fun toStrictAvailabilityNoticeView(item: StrictAvailabilityNotice): StrictAvailabilityNoticeView {
    return StrictAvailabilityNoticeView(
        codeText = item.code,
        titleText = item.title,
        channel = truncate(item.channel, 40),
        ceiling = "%.2f".format(item.ceiling),
        sequenceText = item.sequence.toString(),
        category = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAvailabilityNoticeViews(items: MutableList<StrictAvailabilityNotice>): MutableList<StrictAvailabilityNoticeView> {
    val views = mutableListOf<StrictAvailabilityNoticeView>()
    for (item in items) {
        views.add(toStrictAvailabilityNoticeView(item))
    }
    return views
}

fun sumStrictAvailabilityNoticeView(view: StrictAvailabilityNoticeView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedCeiling + parsedSequence.toDouble()
}
