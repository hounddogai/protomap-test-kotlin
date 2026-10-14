package com.acme.transport.network.cutoff.claim

/** How screens and exports show a cutoff reservation. */
data class CutoffReservationView(
    var code: String,
    var label: String,
    var loadKg: String,
    var category: String,
    var externalRefText: String,
    var capacity: String,
    var attemptsText: String,
    var phone: String,
    var segment: String,
    var driverId: String,
    var statusLabel: String,
)

fun toCutoffReservationView(item: CutoffReservation): CutoffReservationView {
    return CutoffReservationView(
        code = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        category = item.category,
        externalRefText = item.externalRef,
        capacity = item.capacity.toString(),
        attemptsText = item.attempts.toString(),
        phone = item.phone,
        segment = item.segment,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffReservationViews(items: MutableList<CutoffReservation>): MutableList<CutoffReservationView> {
    val views = mutableListOf<CutoffReservationView>()
    for (item in items) {
        views.add(toCutoffReservationView(item))
    }
    return views
}

fun sumCutoffReservationView(view: CutoffReservationView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedLoadKg + parsedCapacity.toDouble() + parsedAttempts.toDouble()
}
