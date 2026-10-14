package com.acme.transport.crewing.restbreak.assignment

/** How screens and exports show a primary rest break notice. */
data class PrimaryRestBreakNoticeView(
    var code: String,
    var name: String,
    var revision: String,
    var createdAt: String,
    var odometer: String,
    var statusLabel: String,
)

fun toPrimaryRestBreakNoticeView(item: PrimaryRestBreakNotice): PrimaryRestBreakNoticeView {
    return PrimaryRestBreakNoticeView(
        code = item.code,
        name = item.name,
        revision = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRestBreakNoticeViews(items: MutableList<PrimaryRestBreakNotice>): MutableList<PrimaryRestBreakNoticeView> {
    val views = mutableListOf<PrimaryRestBreakNoticeView>()
    for (item in items) {
        views.add(toPrimaryRestBreakNoticeView(item))
    }
    return views
}

fun sumPrimaryRestBreakNoticeView(view: PrimaryRestBreakNoticeView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRevision.toDouble() + parsedCreatedAt.toDouble() + parsedOdometer.toDouble()
}
