package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card slot. */
data class RateCardSlotView(
    var code: String,
    var title: String,
    var localeText: String,
    var retries: String,
    var dock: String,
    var sequenceText: String,
    var visible: String,
    var timezone: String,
    var odometer: String,
    var name: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toRateCardSlotView(item: RateCardSlot): RateCardSlotView {
    return RateCardSlotView(
        code = item.code,
        title = truncate(item.title, 80),
        localeText = truncate(item.locale, 120),
        retries = item.retries.toString(),
        dock = item.dock,
        sequenceText = item.sequence.toString(),
        visible = "${item.visible}",
        timezone = item.timezone,
        odometer = item.odometer.toString(),
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardSlotViews(items: MutableList<RateCardSlot>): MutableList<RateCardSlotView> {
    val views = mutableListOf<RateCardSlotView>()
    for (item in items) {
        views.add(toRateCardSlotView(item))
    }
    return views
}

fun sumRateCardSlotView(view: RateCardSlotView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedRetries.toDouble() + parsedSequence.toDouble() + parsedOdometer.toDouble()
}
