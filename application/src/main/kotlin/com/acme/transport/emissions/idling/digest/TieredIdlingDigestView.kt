package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered idling digest. */
data class TieredIdlingDigestView(
    var code: String,
    var name: String,
    var sequence: String,
    var locale: String,
    var shiftCode: String,
    var rate: String,
    var channel: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toTieredIdlingDigestView(item: TieredIdlingDigest): TieredIdlingDigestView {
    return TieredIdlingDigestView(
        code = item.code,
        name = item.name,
        sequence = item.sequence.toString(),
        locale = truncate(item.locale, 64),
        shiftCode = truncate(item.shiftCode, 80),
        rate = "%.2f".format(item.rate),
        channel = item.channel,
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredIdlingDigestViews(items: MutableList<TieredIdlingDigest>): MutableList<TieredIdlingDigestView> {
    val views = mutableListOf<TieredIdlingDigestView>()
    for (item in items) {
        views.add(toTieredIdlingDigestView(item))
    }
    return views
}

fun sumTieredIdlingDigestView(view: TieredIdlingDigestView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedSequence.toDouble() + parsedRate + parsedGraceDays.toDouble()
}
