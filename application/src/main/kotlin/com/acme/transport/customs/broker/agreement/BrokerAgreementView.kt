package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker agreement. */
data class BrokerAgreementView(
    var code: String,
    var name: String,
    var odometer: String,
    var trailerIdText: String,
    var scoreText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toBrokerAgreementView(item: BrokerAgreement): BrokerAgreementView {
    return BrokerAgreementView(
        code = truncate(item.code, 40),
        name = item.name,
        odometer = item.odometer.toString(),
        trailerIdText = item.trailerId,
        scoreText = "%.2f".format(item.score),
        categoryText = truncate(item.category, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerAgreementViews(items: MutableList<BrokerAgreement>): MutableList<BrokerAgreementView> {
    val views = mutableListOf<BrokerAgreementView>()
    for (item in items) {
        views.add(toBrokerAgreementView(item))
    }
    return views
}

fun sumBrokerAgreementView(view: BrokerAgreementView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedOdometer.toDouble() + parsedScore
}
