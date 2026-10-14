package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback camera event summary. */
data class FallbackCameraEventSummaryView(
    var code: String,
    var label: String,
    var checksum: String,
    var timezoneText: String,
    var countryText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toFallbackCameraEventSummaryView(item: FallbackCameraEventSummary): FallbackCameraEventSummaryView {
    return FallbackCameraEventSummaryView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 80),
        checksum = truncate(item.checksum, 64),
        timezoneText = item.timezone,
        countryText = item.country,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCameraEventSummaryViews(items: MutableList<FallbackCameraEventSummary>): MutableList<FallbackCameraEventSummaryView> {
    val views = mutableListOf<FallbackCameraEventSummaryView>()
    for (item in items) {
        views.add(toFallbackCameraEventSummaryView(item))
    }
    return views
}

fun sumFallbackCameraEventSummaryView(view: FallbackCameraEventSummaryView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
