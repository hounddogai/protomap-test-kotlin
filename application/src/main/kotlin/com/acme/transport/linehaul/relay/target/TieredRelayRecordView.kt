package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered relay record. */
data class TieredRelayRecordView(
    var code: String,
    var name: String,
    var createdAt: String,
    var revisionText: String,
    var localeText: String,
    var stopCount: String,
    var sequence: String,
    var statusLabel: String,
)

fun toTieredRelayRecordView(item: TieredRelayRecord): TieredRelayRecordView {
    return TieredRelayRecordView(
        code = truncate(item.code, 80),
        name = item.name,
        createdAt = item.createdAt.toString(),
        revisionText = item.revision.toString(),
        localeText = item.locale,
        stopCount = item.stopCount.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRelayRecordViews(items: MutableList<TieredRelayRecord>): MutableList<TieredRelayRecordView> {
    val views = mutableListOf<TieredRelayRecordView>()
    for (item in items) {
        views.add(toTieredRelayRecordView(item))
    }
    return views
}

fun sumTieredRelayRecordView(view: TieredRelayRecordView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCreatedAt.toDouble() + parsedRevision.toDouble() + parsedStopCount.toDouble()
}
