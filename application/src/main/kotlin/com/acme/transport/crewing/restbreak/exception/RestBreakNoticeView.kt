package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break notice. */
data class RestBreakNoticeView(
    var code: String,
    var name: String,
    var titleText: String,
    var phone: String,
    var approved: String,
    var revisionText: String,
    var labelText: String,
    var windowDays: String,
    var etaMinutes: String,
    var description: String,
    var statusLabel: String,
)

fun toRestBreakNoticeView(item: RestBreakNotice): RestBreakNoticeView {
    return RestBreakNoticeView(
        code = item.code,
        name = truncate(item.name, 40),
        titleText = truncate(item.title, 120),
        phone = truncate(item.phone, 40),
        approved = "${item.approved}",
        revisionText = item.revision.toString(),
        labelText = truncate(item.label, 80),
        windowDays = item.windowDays.toString(),
        etaMinutes = item.etaMinutes.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakNoticeViews(items: MutableList<RestBreakNotice>): MutableList<RestBreakNoticeView> {
    val views = mutableListOf<RestBreakNoticeView>()
    for (item in items) {
        views.add(toRestBreakNoticeView(item))
    }
    return views
}

fun sumRestBreakNoticeView(view: RestBreakNoticeView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRevision.toDouble() + parsedWindowDays.toDouble() + parsedEtaMinutes.toDouble()
}
