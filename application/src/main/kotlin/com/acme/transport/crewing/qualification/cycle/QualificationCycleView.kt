package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification cycle. */
data class QualificationCycleView(
    var code: String,
    var title: String,
    var quantityText: String,
    var source: String,
    var odometer: String,
    var graceDaysText: String,
    var currency: String,
    var statusLabel: String,
)

fun toQualificationCycleView(item: QualificationCycle): QualificationCycleView {
    return QualificationCycleView(
        code = item.code,
        title = item.title,
        quantityText = item.quantity.toString(),
        source = truncate(item.source, 64),
        odometer = item.odometer.toString(),
        graceDaysText = item.graceDays.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationCycleViews(items: MutableList<QualificationCycle>): MutableList<QualificationCycleView> {
    val views = mutableListOf<QualificationCycleView>()
    for (item in items) {
        views.add(toQualificationCycleView(item))
    }
    return views
}

fun sumQualificationCycleView(view: QualificationCycleView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedQuantity.toDouble() + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
