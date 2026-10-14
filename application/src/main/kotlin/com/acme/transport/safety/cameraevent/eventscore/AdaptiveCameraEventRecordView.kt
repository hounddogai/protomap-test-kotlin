package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive camera event record. */
data class AdaptiveCameraEventRecordView(
    var code: String,
    var title: String,
    var shiftCodeText: String,
    var minimum: String,
    var quantityText: String,
    var phoneText: String,
    var dock: String,
    var countryText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toAdaptiveCameraEventRecordView(item: AdaptiveCameraEventRecord): AdaptiveCameraEventRecordView {
    return AdaptiveCameraEventRecordView(
        code = item.code,
        title = truncate(item.title, 80),
        shiftCodeText = truncate(item.shiftCode, 64),
        minimum = "%.2f".format(item.minimum),
        quantityText = item.quantity.toString(),
        phoneText = item.phone,
        dock = item.dock,
        countryText = item.country,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCameraEventRecordViews(items: MutableList<AdaptiveCameraEventRecord>): MutableList<AdaptiveCameraEventRecordView> {
    val views = mutableListOf<AdaptiveCameraEventRecordView>()
    for (item in items) {
        views.add(toAdaptiveCameraEventRecordView(item))
    }
    return views
}

fun sumAdaptiveCameraEventRecordView(view: AdaptiveCameraEventRecordView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedMinimum + parsedQuantity.toDouble() + parsedEndsAt.toDouble()
}
