package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification record. */
data class QualificationRecordView(
    var code: String,
    var name: String,
    var loadKg: String,
    var retriesText: String,
    var distanceKmText: String,
    var lane: String,
    var version: String,
    var discount: String,
    var currencyText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toQualificationRecordView(item: QualificationRecord): QualificationRecordView {
    return QualificationRecordView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 120),
        loadKg = "%.2f".format(item.loadKg),
        retriesText = item.retries.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        lane = item.lane,
        version = item.version.toString(),
        discount = "%.2f".format(item.discount),
        currencyText = item.currency,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationRecordViews(items: MutableList<QualificationRecord>): MutableList<QualificationRecordView> {
    val views = mutableListOf<QualificationRecordView>()
    for (item in items) {
        views.add(toQualificationRecordView(item))
    }
    return views
}

fun sumQualificationRecordView(view: QualificationRecordView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedLoadKg + parsedRetries.toDouble() + parsedDistanceKm
}
