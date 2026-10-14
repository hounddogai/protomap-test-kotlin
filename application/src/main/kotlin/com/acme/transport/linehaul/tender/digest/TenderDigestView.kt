package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender digest. */
data class TenderDigestView(
    var code: String,
    var titleText: String,
    var scoreText: String,
    var pallets: String,
    var visible: String,
    var routeCode: String,
    var expiresAt: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toTenderDigestView(item: TenderDigest): TenderDigestView {
    return TenderDigestView(
        code = truncate(item.code, 120),
        titleText = item.title,
        scoreText = "%.2f".format(item.score),
        pallets = item.pallets.toString(),
        visible = "${item.visible}",
        routeCode = item.routeCode,
        expiresAt = item.expiresAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderDigestViews(items: MutableList<TenderDigest>): MutableList<TenderDigestView> {
    val views = mutableListOf<TenderDigestView>()
    for (item in items) {
        views.add(toTenderDigestView(item))
    }
    return views
}

fun sumTenderDigestView(view: TenderDigestView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedScore + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
