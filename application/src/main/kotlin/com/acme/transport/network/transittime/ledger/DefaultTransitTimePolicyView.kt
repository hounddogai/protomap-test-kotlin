package com.acme.transport.network.transittime.ledger

/** How screens and exports show a default transit time policy. */
data class DefaultTransitTimePolicyView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var distanceKm: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDefaultTransitTimePolicyView(item: DefaultTransitTimePolicy): DefaultTransitTimePolicyView {
    return DefaultTransitTimePolicyView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTransitTimePolicyViews(items: MutableList<DefaultTransitTimePolicy>): MutableList<DefaultTransitTimePolicyView> {
    val views = mutableListOf<DefaultTransitTimePolicyView>()
    for (item in items) {
        views.add(toDefaultTransitTimePolicyView(item))
    }
    return views
}

fun sumDefaultTransitTimePolicyView(view: DefaultTransitTimePolicyView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedDistanceKm + parsedEndsAt.toDouble()
}
