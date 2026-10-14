package com.acme.transport.freightpay.audit.legacyauditpolicy

/** How screens and exports show a capped audit digest. */
data class CappedAuditDigestView(
    var code: String,
    var name: String,
    var revision: String,
    var cost: String,
    var score: String,
    var minimum: String,
    var reviewerText: String,
    var weight: String,
    var statusLabel: String,
)

fun toCappedAuditDigestView(item: CappedAuditDigest): CappedAuditDigestView {
    return CappedAuditDigestView(
        code = item.code,
        name = item.name,
        revision = item.revision.toString(),
        cost = "%.2f".format(item.cost),
        score = "%.2f".format(item.score),
        minimum = "%.2f".format(item.minimum),
        reviewerText = item.reviewer,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAuditDigestViews(items: MutableList<CappedAuditDigest>): MutableList<CappedAuditDigestView> {
    val views = mutableListOf<CappedAuditDigestView>()
    for (item in items) {
        views.add(toCappedAuditDigestView(item))
    }
    return views
}

fun sumCappedAuditDigestView(view: CappedAuditDigestView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRevision.toDouble() + parsedCost + parsedScore
}
