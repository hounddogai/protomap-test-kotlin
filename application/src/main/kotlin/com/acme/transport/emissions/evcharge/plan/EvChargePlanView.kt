package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge plan. */
data class EvChargePlanView(
    var codeText: String,
    var title: String,
    var country: String,
    var scoreText: String,
    var ownerIdText: String,
    var laneText: String,
    var revisionText: String,
    var attemptsText: String,
    var approvedText: String,
    var weight: String,
    var capacity: String,
    var statusLabel: String,
)

fun toEvChargePlanView(item: EvChargePlan): EvChargePlanView {
    return EvChargePlanView(
        codeText = item.code,
        title = item.title,
        country = truncate(item.country, 40),
        scoreText = "%.2f".format(item.score),
        ownerIdText = item.ownerId,
        laneText = item.lane,
        revisionText = item.revision.toString(),
        attemptsText = item.attempts.toString(),
        approvedText = "${item.approved}",
        weight = "%.2f".format(item.weight),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargePlanViews(items: MutableList<EvChargePlan>): MutableList<EvChargePlanView> {
    val views = mutableListOf<EvChargePlanView>()
    for (item in items) {
        views.add(toEvChargePlanView(item))
    }
    return views
}

fun sumEvChargePlanView(view: EvChargePlanView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedScore + parsedRevision.toDouble() + parsedAttempts.toDouble()
}
