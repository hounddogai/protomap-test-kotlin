package com.acme.transport.network.zone.claim

/** How screens and exports show a capped zone claim. */
data class CappedZoneClaimView(
    var codeText: String,
    var name: String,
    var trailerId: String,
    var minimum: String,
    var amount: String,
    var scoreText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toCappedZoneClaimView(item: CappedZoneClaim): CappedZoneClaimView {
    return CappedZoneClaimView(
        codeText = item.code,
        name = item.name,
        trailerId = item.trailerId,
        minimum = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        scoreText = "%.2f".format(item.score),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedZoneClaimViews(items: MutableList<CappedZoneClaim>): MutableList<CappedZoneClaimView> {
    val views = mutableListOf<CappedZoneClaimView>()
    for (item in items) {
        views.add(toCappedZoneClaimView(item))
    }
    return views
}

fun sumCappedZoneClaimView(view: CappedZoneClaimView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedMinimum + parsedAmount + parsedScore
}
