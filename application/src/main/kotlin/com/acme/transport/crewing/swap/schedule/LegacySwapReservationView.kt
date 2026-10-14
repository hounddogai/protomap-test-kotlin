package com.acme.transport.crewing.swap.schedule

/** How screens and exports show a legacy swap reservation. */
data class LegacySwapReservationView(
    var code: String,
    var label: String,
    var amountText: String,
    var ceilingText: String,
    var country: String,
    var statusLabel: String,
)

fun toLegacySwapReservationView(item: LegacySwapReservation): LegacySwapReservationView {
    return LegacySwapReservationView(
        code = item.code,
        label = item.label,
        amountText = "%.2f".format(item.amount),
        ceilingText = "%.2f".format(item.ceiling),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacySwapReservationViews(items: MutableList<LegacySwapReservation>): MutableList<LegacySwapReservationView> {
    val views = mutableListOf<LegacySwapReservationView>()
    for (item in items) {
        views.add(toLegacySwapReservationView(item))
    }
    return views
}

fun sumLegacySwapReservationView(view: LegacySwapReservationView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedAmount + parsedCeiling
}
