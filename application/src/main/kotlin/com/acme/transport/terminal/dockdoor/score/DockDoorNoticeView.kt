package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door notice. */
data class DockDoorNoticeView(
    var codeText: String,
    var label: String,
    var ratio: String,
    var locale: String,
    var retries: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toDockDoorNoticeView(item: DockDoorNotice): DockDoorNoticeView {
    return DockDoorNoticeView(
        codeText = item.code,
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        locale = item.locale,
        retries = item.retries.toString(),
        contactEmail = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorNoticeViews(items: MutableList<DockDoorNotice>): MutableList<DockDoorNoticeView> {
    val views = mutableListOf<DockDoorNoticeView>()
    for (item in items) {
        views.add(toDockDoorNoticeView(item))
    }
    return views
}

fun sumDockDoorNoticeView(view: DockDoorNoticeView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedRatio + parsedRetries.toDouble()
}
