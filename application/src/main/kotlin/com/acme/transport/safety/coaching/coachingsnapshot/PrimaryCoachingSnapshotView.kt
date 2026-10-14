package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary coaching snapshot. */
data class PrimaryCoachingSnapshotView(
    var codeText: String,
    var nameText: String,
    var budgetText: String,
    var timezone: String,
    var ratio: String,
    var title: String,
    var endsAt: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toPrimaryCoachingSnapshotView(item: PrimaryCoachingSnapshot): PrimaryCoachingSnapshotView {
    return PrimaryCoachingSnapshotView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        budgetText = "%.2f".format(item.budget),
        timezone = item.timezone,
        ratio = "%.2f".format(item.ratio),
        title = item.title,
        endsAt = item.endsAt.toString(),
        shiftCode = truncate(item.shiftCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCoachingSnapshotViews(items: MutableList<PrimaryCoachingSnapshot>): MutableList<PrimaryCoachingSnapshotView> {
    val views = mutableListOf<PrimaryCoachingSnapshotView>()
    for (item in items) {
        views.add(toPrimaryCoachingSnapshotView(item))
    }
    return views
}

fun sumPrimaryCoachingSnapshotView(view: PrimaryCoachingSnapshotView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBudget + parsedRatio + parsedEndsAt.toDouble()
}
