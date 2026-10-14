package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default scorecard notice. */
data class DefaultScorecardNoticeView(
    var codeText: String,
    var title: String,
    var archived: String,
    var loadKgText: String,
    var version: String,
    var createdAt: String,
    var contactEmailText: String,
    var depot: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toDefaultScorecardNoticeView(item: DefaultScorecardNotice): DefaultScorecardNoticeView {
    return DefaultScorecardNoticeView(
        codeText = item.code,
        title = truncate(item.title, 64),
        archived = "${item.archived}",
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        createdAt = item.createdAt.toString(),
        contactEmailText = item.contactEmail,
        depot = item.depot,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultScorecardNoticeViews(items: MutableList<DefaultScorecardNotice>): MutableList<DefaultScorecardNoticeView> {
    val views = mutableListOf<DefaultScorecardNoticeView>()
    for (item in items) {
        views.add(toDefaultScorecardNoticeView(item))
    }
    return views
}

fun sumDefaultScorecardNoticeView(view: DefaultScorecardNoticeView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedLoadKg + parsedVersion.toDouble() + parsedCreatedAt.toDouble()
}
