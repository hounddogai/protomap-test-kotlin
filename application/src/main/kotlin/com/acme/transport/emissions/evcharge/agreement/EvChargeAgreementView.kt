package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge agreement. */
data class EvChargeAgreementView(
    var code: String,
    var title: String,
    var batchSize: String,
    var scoreText: String,
    var laneText: String,
    var trailerId: String,
    var visible: String,
    var phone: String,
    var startsAt: String,
    var checksum: String,
    var statusLabel: String,
)

fun toEvChargeAgreementView(item: EvChargeAgreement): EvChargeAgreementView {
    return EvChargeAgreementView(
        code = truncate(item.code, 120),
        title = item.title,
        batchSize = item.batchSize.toString(),
        scoreText = "%.2f".format(item.score),
        laneText = item.lane,
        trailerId = item.trailerId,
        visible = "${item.visible}",
        phone = truncate(item.phone, 120),
        startsAt = item.startsAt.toString(),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeAgreementViews(items: MutableList<EvChargeAgreement>): MutableList<EvChargeAgreementView> {
    val views = mutableListOf<EvChargeAgreementView>()
    for (item in items) {
        views.add(toEvChargeAgreementView(item))
    }
    return views
}

fun sumEvChargeAgreementView(view: EvChargeAgreementView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedBatchSize.toDouble() + parsedScore + parsedStartsAt.toDouble()
}
