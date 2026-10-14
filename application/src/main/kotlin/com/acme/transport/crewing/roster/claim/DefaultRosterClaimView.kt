package com.acme.transport.crewing.roster.claim

/** How screens and exports show a default roster claim. */
data class DefaultRosterClaimView(
    var codeText: String,
    var label: String,
    var retries: String,
    var createdAtText: String,
    var distanceKm: String,
    var externalRef: String,
    var currency: String,
    var stopCountText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toDefaultRosterClaimView(item: DefaultRosterClaim): DefaultRosterClaimView {
    return DefaultRosterClaimView(
        codeText = item.code,
        label = item.label,
        retries = item.retries.toString(),
        createdAtText = item.createdAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        currency = item.currency,
        stopCountText = item.stopCount.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRosterClaimViews(items: MutableList<DefaultRosterClaim>): MutableList<DefaultRosterClaimView> {
    val views = mutableListOf<DefaultRosterClaimView>()
    for (item in items) {
        views.add(toDefaultRosterClaimView(item))
    }
    return views
}

fun sumDefaultRosterClaimView(view: DefaultRosterClaimView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRetries.toDouble() + parsedCreatedAt.toDouble() + parsedDistanceKm
}
