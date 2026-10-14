package com.acme.transport.freightpay.ratecard.review

/** How screens and exports show a rate card record. */
data class RateCardRecordView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var region: String,
    var score: String,
    var rate: String,
    var driverId: String,
    var archivedText: String,
    var active: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toRateCardRecordView(item: RateCardRecord): RateCardRecordView {
    return RateCardRecordView(
        code = item.code,
        nameText = item.name,
        quantity = item.quantity.toString(),
        region = item.region,
        score = "%.2f".format(item.score),
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        archivedText = "${item.archived}",
        active = "${item.active}",
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardRecordViews(items: MutableList<RateCardRecord>): MutableList<RateCardRecordView> {
    val views = mutableListOf<RateCardRecordView>()
    for (item in items) {
        views.add(toRateCardRecordView(item))
    }
    return views
}

fun sumRateCardRecordView(view: RateCardRecordView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedQuantity.toDouble() + parsedScore + parsedRate
}
