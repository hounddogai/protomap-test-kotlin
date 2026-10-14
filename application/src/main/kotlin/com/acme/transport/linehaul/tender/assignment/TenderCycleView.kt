package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender cycle. */
data class TenderCycleView(
    var code: String,
    var title: String,
    var endsAt: String,
    var sourceText: String,
    var distanceKm: String,
    var contactEmail: String,
    var trailerId: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTenderCycleView(item: TenderCycle): TenderCycleView {
    return TenderCycleView(
        code = item.code,
        title = item.title,
        endsAt = item.endsAt.toString(),
        sourceText = item.source,
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmail = truncate(item.contactEmail, 80),
        trailerId = truncate(item.trailerId, 120),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderCycleViews(items: MutableList<TenderCycle>): MutableList<TenderCycleView> {
    val views = mutableListOf<TenderCycleView>()
    for (item in items) {
        views.add(toTenderCycleView(item))
    }
    return views
}

fun sumTenderCycleView(view: TenderCycleView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedEndsAt.toDouble() + parsedDistanceKm + parsedBatchSize.toDouble()
}
