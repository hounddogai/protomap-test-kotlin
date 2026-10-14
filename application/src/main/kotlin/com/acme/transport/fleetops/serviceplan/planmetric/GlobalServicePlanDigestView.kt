package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global service plan digest. */
data class GlobalServicePlanDigestView(
    var code: String,
    var title: String,
    var score: String,
    var threshold: String,
    var minimumText: String,
    var endsAtText: String,
    var lane: String,
    var timezone: String,
    var approved: String,
    var statusLabel: String,
)

fun toGlobalServicePlanDigestView(item: GlobalServicePlanDigest): GlobalServicePlanDigestView {
    return GlobalServicePlanDigestView(
        code = truncate(item.code, 80),
        title = item.title,
        score = "%.2f".format(item.score),
        threshold = "%.2f".format(item.threshold),
        minimumText = "%.2f".format(item.minimum),
        endsAtText = item.endsAt.toString(),
        lane = truncate(item.lane, 120),
        timezone = item.timezone,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalServicePlanDigestViews(items: MutableList<GlobalServicePlanDigest>): MutableList<GlobalServicePlanDigestView> {
    val views = mutableListOf<GlobalServicePlanDigestView>()
    for (item in items) {
        views.add(toGlobalServicePlanDigestView(item))
    }
    return views
}

fun sumGlobalServicePlanDigestView(view: GlobalServicePlanDigestView): Double {
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedScore + parsedThreshold + parsedMinimum
}
