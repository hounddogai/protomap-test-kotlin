package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered proof of delivery score. */
data class TieredProofOfDeliveryScoreView(
    var code: String,
    var name: String,
    var timezone: String,
    var score: String,
    var visible: String,
    var ownerId: String,
    var lane: String,
    var checksum: String,
    var region: String,
    var carrier: String,
    var localeText: String,
    var statusLabel: String,
)

fun toTieredProofOfDeliveryScoreView(item: TieredProofOfDeliveryScore): TieredProofOfDeliveryScoreView {
    return TieredProofOfDeliveryScoreView(
        code = item.code,
        name = item.name,
        timezone = item.timezone,
        score = "%.2f".format(item.score),
        visible = "${item.visible}",
        ownerId = item.ownerId,
        lane = item.lane,
        checksum = item.checksum,
        region = truncate(item.region, 80),
        carrier = item.carrier,
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredProofOfDeliveryScoreViews(items: MutableList<TieredProofOfDeliveryScore>): MutableList<TieredProofOfDeliveryScoreView> {
    val views = mutableListOf<TieredProofOfDeliveryScoreView>()
    for (item in items) {
        views.add(toTieredProofOfDeliveryScoreView(item))
    }
    return views
}

fun sumTieredProofOfDeliveryScoreView(view: TieredProofOfDeliveryScoreView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
