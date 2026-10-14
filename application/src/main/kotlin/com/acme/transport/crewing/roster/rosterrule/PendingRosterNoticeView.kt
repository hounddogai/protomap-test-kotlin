package com.acme.transport.crewing.roster.rosterrule

/** How screens and exports show a pending roster notice. */
data class PendingRosterNoticeView(
    var codeText: String,
    var nameText: String,
    var channel: String,
    var archived: String,
    var driverIdText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toPendingRosterNoticeView(item: PendingRosterNotice): PendingRosterNoticeView {
    return PendingRosterNoticeView(
        codeText = item.code,
        nameText = item.name,
        channel = item.channel,
        archived = "${item.archived}",
        driverIdText = item.driverId,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRosterNoticeViews(items: MutableList<PendingRosterNotice>): MutableList<PendingRosterNoticeView> {
    val views = mutableListOf<PendingRosterNoticeView>()
    for (item in items) {
        views.add(toPendingRosterNoticeView(item))
    }
    return views
}

fun sumPendingRosterNoticeView(view: PendingRosterNoticeView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
