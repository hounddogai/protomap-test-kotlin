package com.acme.transport.lastmile.redelivery.redeliverywindow

/** How screens and exports show a scheduled redelivery profile. */
data class ScheduledRedeliveryProfileView(
    var code: String,
    var titleText: String,
    var checksumText: String,
    var approvedText: String,
    var score: String,
    var statusLabel: String,
)

fun toScheduledRedeliveryProfileView(item: ScheduledRedeliveryProfile): ScheduledRedeliveryProfileView {
    return ScheduledRedeliveryProfileView(
        code = item.code,
        titleText = item.title,
        checksumText = item.checksum,
        approvedText = "${item.approved}",
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRedeliveryProfileViews(items: MutableList<ScheduledRedeliveryProfile>): MutableList<ScheduledRedeliveryProfileView> {
    val views = mutableListOf<ScheduledRedeliveryProfileView>()
    for (item in items) {
        views.add(toScheduledRedeliveryProfileView(item))
    }
    return views
}

fun sumScheduledRedeliveryProfileView(view: ScheduledRedeliveryProfileView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
