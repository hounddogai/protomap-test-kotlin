package com.acme.transport.safety.scorecard.scorecardnotice

/** How screens and exports show a scorecard group. */
data class ScorecardGroupView(
    var code: String,
    var nameText: String,
    var ownerIdText: String,
    var pinnedText: String,
    var phone: String,
    var score: String,
    var statusLabel: String,
)

fun toScorecardGroupView(item: ScorecardGroup): ScorecardGroupView {
    return ScorecardGroupView(
        code = item.code,
        nameText = item.name,
        ownerIdText = item.ownerId,
        pinnedText = "${item.pinned}",
        phone = item.phone,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardGroupViews(items: MutableList<ScorecardGroup>): MutableList<ScorecardGroupView> {
    val views = mutableListOf<ScorecardGroupView>()
    for (item in items) {
        views.add(toScorecardGroupView(item))
    }
    return views
}

fun sumScorecardGroupView(view: ScorecardGroupView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
