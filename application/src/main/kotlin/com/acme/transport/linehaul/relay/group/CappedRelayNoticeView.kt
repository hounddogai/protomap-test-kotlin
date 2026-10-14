package com.acme.transport.linehaul.relay.group

/** How screens and exports show a capped relay notice. */
data class CappedRelayNoticeView(
    var code: String,
    var title: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toCappedRelayNoticeView(item: CappedRelayNotice): CappedRelayNoticeView {
    return CappedRelayNoticeView(
        code = item.code,
        title = item.title,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRelayNoticeViews(items: MutableList<CappedRelayNotice>): MutableList<CappedRelayNoticeView> {
    val views = mutableListOf<CappedRelayNoticeView>()
    for (item in items) {
        views.add(toCappedRelayNoticeView(item))
    }
    return views
}

fun sumCappedRelayNoticeView(view: CappedRelayNoticeView): Double {
    val parsedPriority = view.priorityText.toInt()
    return parsedPriority.toDouble()
}
