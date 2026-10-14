package com.acme.transport.lastmile.drop.dropreservation

/** How screens and exports show a strict drop audit. */
data class StrictDropAuditView(
    var code: String,
    var title: String,
    var archivedText: String,
    var summaryText: String,
    var depot: String,
    var score: String,
    var statusLabel: String,
)

fun toStrictDropAuditView(item: StrictDropAudit): StrictDropAuditView {
    return StrictDropAuditView(
        code = item.code,
        title = item.title,
        archivedText = "${item.archived}",
        summaryText = item.summary,
        depot = item.depot,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDropAuditViews(items: MutableList<StrictDropAudit>): MutableList<StrictDropAuditView> {
    val views = mutableListOf<StrictDropAuditView>()
    for (item in items) {
        views.add(toStrictDropAuditView(item))
    }
    return views
}

fun sumStrictDropAuditView(view: StrictDropAuditView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
