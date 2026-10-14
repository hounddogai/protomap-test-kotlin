package com.acme.transport.freightpay.audit.allocation

/** How screens and exports show a strict audit claim. */
data class StrictAuditClaimView(
    var code: String,
    var name: String,
    var cost: String,
    var ratio: String,
    var retriesText: String,
    var approvedText: String,
    var discountText: String,
    var region: String,
    var minimum: String,
    var statusLabel: String,
)

fun toStrictAuditClaimView(item: StrictAuditClaim): StrictAuditClaimView {
    return StrictAuditClaimView(
        code = item.code,
        name = item.name,
        cost = "%.2f".format(item.cost),
        ratio = "%.2f".format(item.ratio),
        retriesText = item.retries.toString(),
        approvedText = "${item.approved}",
        discountText = "%.2f".format(item.discount),
        region = item.region,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAuditClaimViews(items: MutableList<StrictAuditClaim>): MutableList<StrictAuditClaimView> {
    val views = mutableListOf<StrictAuditClaimView>()
    for (item in items) {
        views.add(toStrictAuditClaimView(item))
    }
    return views
}

fun sumStrictAuditClaimView(view: StrictAuditClaimView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedCost + parsedRatio + parsedRetries.toDouble()
}
