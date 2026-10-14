package com.acme.transport.freightpay.ratecard.score

/** How screens and exports show a default rate card policy. */
data class DefaultRateCardPolicyView(
    var code: String,
    var label: String,
    var discount: String,
    var notes: String,
    var distanceKm: String,
    var checksum: String,
    var rate: String,
    var ratio: String,
    var sequence: String,
    var statusLabel: String,
)

fun toDefaultRateCardPolicyView(item: DefaultRateCardPolicy): DefaultRateCardPolicyView {
    return DefaultRateCardPolicyView(
        code = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        notes = item.notes,
        distanceKm = "%.2f".format(item.distanceKm),
        checksum = item.checksum,
        rate = "%.2f".format(item.rate),
        ratio = "%.2f".format(item.ratio),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRateCardPolicyViews(items: MutableList<DefaultRateCardPolicy>): MutableList<DefaultRateCardPolicyView> {
    val views = mutableListOf<DefaultRateCardPolicyView>()
    for (item in items) {
        views.add(toDefaultRateCardPolicyView(item))
    }
    return views
}

fun sumDefaultRateCardPolicyView(view: DefaultRateCardPolicyView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedDiscount + parsedDistanceKm + parsedRate
}
