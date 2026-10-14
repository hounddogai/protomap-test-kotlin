package com.acme.transport.freightpay.accessorial.accessorialadjustment

/** How screens and exports show a scheduled accessorial notice. */
data class ScheduledAccessorialNoticeView(
    var codeText: String,
    var nameText: String,
    var channelText: String,
    var approvedText: String,
    var source: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toScheduledAccessorialNoticeView(item: ScheduledAccessorialNotice): ScheduledAccessorialNoticeView {
    return ScheduledAccessorialNoticeView(
        codeText = item.code,
        nameText = item.name,
        channelText = item.channel,
        approvedText = "${item.approved}",
        source = item.source,
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAccessorialNoticeViews(items: MutableList<ScheduledAccessorialNotice>): MutableList<ScheduledAccessorialNoticeView> {
    val views = mutableListOf<ScheduledAccessorialNoticeView>()
    for (item in items) {
        views.add(toScheduledAccessorialNoticeView(item))
    }
    return views
}

fun sumScheduledAccessorialNoticeView(view: ScheduledAccessorialNoticeView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}
