package com.acme.transport.linehaul.relay.claim

/** How screens and exports show a fallback relay claim. */
data class FallbackRelayClaimView(
    var code: String,
    var nameText: String,
    var phone: String,
    var minimum: String,
    var graceDaysText: String,
    var weight: String,
    var title: String,
    var statusLabel: String,
)

fun toFallbackRelayClaimView(item: FallbackRelayClaim): FallbackRelayClaimView {
    return FallbackRelayClaimView(
        code = item.code,
        nameText = item.name,
        phone = item.phone,
        minimum = "%.2f".format(item.minimum),
        graceDaysText = item.graceDays.toString(),
        weight = "%.2f".format(item.weight),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRelayClaimViews(items: MutableList<FallbackRelayClaim>): MutableList<FallbackRelayClaimView> {
    val views = mutableListOf<FallbackRelayClaimView>()
    for (item in items) {
        views.add(toFallbackRelayClaimView(item))
    }
    return views
}

fun sumFallbackRelayClaimView(view: FallbackRelayClaimView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedMinimum + parsedGraceDays.toDouble() + parsedWeight
}
