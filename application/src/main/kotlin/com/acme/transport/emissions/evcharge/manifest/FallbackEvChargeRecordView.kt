package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback ev charge record. */
data class FallbackEvChargeRecordView(
    var codeText: String,
    var title: String,
    var channel: String,
    var priority: String,
    var description: String,
    var segment: String,
    var currency: String,
    var statusLabel: String,
)

fun toFallbackEvChargeRecordView(item: FallbackEvChargeRecord): FallbackEvChargeRecordView {
    return FallbackEvChargeRecordView(
        codeText = truncate(item.code, 80),
        title = item.title,
        channel = truncate(item.channel, 80),
        priority = item.priority.toString(),
        description = truncate(item.description, 80),
        segment = truncate(item.segment, 80),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEvChargeRecordViews(items: MutableList<FallbackEvChargeRecord>): MutableList<FallbackEvChargeRecordView> {
    val views = mutableListOf<FallbackEvChargeRecordView>()
    for (item in items) {
        views.add(toFallbackEvChargeRecordView(item))
    }
    return views
}

fun sumFallbackEvChargeRecordView(view: FallbackEvChargeRecordView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
