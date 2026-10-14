package com.acme.transport.linehaul.tender.tenderforecast

/** How screens and exports show a capped tender claim. */
data class CappedTenderClaimView(
    var code: String,
    var name: String,
    var activeText: String,
    var etaMinutes: String,
    var margin: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toCappedTenderClaimView(item: CappedTenderClaim): CappedTenderClaimView {
    return CappedTenderClaimView(
        code = item.code,
        name = item.name,
        activeText = "${item.active}",
        etaMinutes = item.etaMinutes.toString(),
        margin = "%.2f".format(item.margin),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTenderClaimViews(items: MutableList<CappedTenderClaim>): MutableList<CappedTenderClaimView> {
    val views = mutableListOf<CappedTenderClaimView>()
    for (item in items) {
        views.add(toCappedTenderClaimView(item))
    }
    return views
}

fun sumCappedTenderClaimView(view: CappedTenderClaimView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedEtaMinutes.toDouble() + parsedMargin + parsedStartsAt.toDouble()
}
