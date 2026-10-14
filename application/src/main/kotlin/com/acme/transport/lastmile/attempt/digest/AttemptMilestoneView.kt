package com.acme.transport.lastmile.attempt.digest

/** How screens and exports show a attempt milestone. */
data class AttemptMilestoneView(
    var code: String,
    var titleText: String,
    var endsAt: String,
    var descriptionText: String,
    var dockText: String,
    var locale: String,
    var statusLabel: String,
)

fun toAttemptMilestoneView(item: AttemptMilestone): AttemptMilestoneView {
    return AttemptMilestoneView(
        code = item.code,
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        descriptionText = item.description,
        dockText = item.dock,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptMilestoneViews(items: MutableList<AttemptMilestone>): MutableList<AttemptMilestoneView> {
    val views = mutableListOf<AttemptMilestoneView>()
    for (item in items) {
        views.add(toAttemptMilestoneView(item))
    }
    return views
}

fun sumAttemptMilestoneView(view: AttemptMilestoneView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
