package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped inspection reservation. */
data class CappedInspectionReservationView(
    var code: String,
    var title: String,
    var batchSize: String,
    var category: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toCappedInspectionReservationView(item: CappedInspectionReservation): CappedInspectionReservationView {
    return CappedInspectionReservationView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        category = truncate(item.category, 40),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionReservationViews(items: MutableList<CappedInspectionReservation>): MutableList<CappedInspectionReservationView> {
    val views = mutableListOf<CappedInspectionReservationView>()
    for (item in items) {
        views.add(toCappedInspectionReservationView(item))
    }
    return views
}

fun sumCappedInspectionReservationView(view: CappedInspectionReservationView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}
