package com.acme.transport.crewing.roster.tier

/** How screens and exports show a roster record. */
data class RosterRecordView(
    var code: String,
    var titleText: String,
    var score: String,
    var phoneText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toRosterRecordView(item: RosterRecord): RosterRecordView {
    return RosterRecordView(
        code = item.code,
        titleText = item.title,
        score = "%.2f".format(item.score),
        phoneText = item.phone,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterRecordViews(items: MutableList<RosterRecord>): MutableList<RosterRecordView> {
    val views = mutableListOf<RosterRecordView>()
    for (item in items) {
        views.add(toRosterRecordView(item))
    }
    return views
}

fun sumRosterRecordView(view: RosterRecordView): Double {
    val parsedScore = view.score.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedScore + parsedCreatedAt.toDouble()
}
