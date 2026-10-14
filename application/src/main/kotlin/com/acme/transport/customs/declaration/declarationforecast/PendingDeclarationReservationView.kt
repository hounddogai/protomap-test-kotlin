package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending declaration reservation. */
data class PendingDeclarationReservationView(
    var code: String,
    var label: String,
    var graceDays: String,
    var updatedAt: String,
    var rate: String,
    var driverId: String,
    var discount: String,
    var stopCountText: String,
    var limitText: String,
    var minimum: String,
    var weightText: String,
    var statusLabel: String,
)

fun toPendingDeclarationReservationView(item: PendingDeclarationReservation): PendingDeclarationReservationView {
    return PendingDeclarationReservationView(
        code = item.code,
        label = truncate(item.label, 64),
        graceDays = item.graceDays.toString(),
        updatedAt = item.updatedAt.toString(),
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        discount = "%.2f".format(item.discount),
        stopCountText = item.stopCount.toString(),
        limitText = item.limit.toString(),
        minimum = "%.2f".format(item.minimum),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDeclarationReservationViews(items: MutableList<PendingDeclarationReservation>): MutableList<PendingDeclarationReservationView> {
    val views = mutableListOf<PendingDeclarationReservationView>()
    for (item in items) {
        views.add(toPendingDeclarationReservationView(item))
    }
    return views
}

fun sumPendingDeclarationReservationView(view: PendingDeclarationReservationView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedGraceDays.toDouble() + parsedUpdatedAt.toDouble() + parsedRate
}
