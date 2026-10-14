package com.acme.transport.customs.hold.audit

/** How screens and exports show a hold estimate. */
data class HoldEstimateView(
    var codeText: String,
    var name: String,
    var score: String,
    var revision: String,
    var etaMinutesText: String,
    var cost: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toHoldEstimateView(item: HoldEstimate): HoldEstimateView {
    return HoldEstimateView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        revision = item.revision.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        cost = "%.2f".format(item.cost),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldEstimateViews(items: MutableList<HoldEstimate>): MutableList<HoldEstimateView> {
    val views = mutableListOf<HoldEstimateView>()
    for (item in items) {
        views.add(toHoldEstimateView(item))
    }
    return views
}

fun sumHoldEstimateView(view: HoldEstimateView): Double {
    val parsedScore = view.score.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedScore + parsedRevision.toDouble() + parsedEtaMinutes.toDouble()
}
