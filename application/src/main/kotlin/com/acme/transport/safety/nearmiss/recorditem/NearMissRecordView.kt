package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss record. */
data class NearMissRecordView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var name: String,
    var ceiling: String,
    var contactEmail: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toNearMissRecordView(item: NearMissRecord): NearMissRecordView {
    return NearMissRecordView(
        codeText = truncate(item.code, 64),
        title = truncate(item.title, 40),
        durationMinutes = item.durationMinutes.toString(),
        name = truncate(item.name, 40),
        ceiling = "%.2f".format(item.ceiling),
        contactEmail = truncate(item.contactEmail, 64),
        currencyText = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissRecordViews(items: MutableList<NearMissRecord>): MutableList<NearMissRecordView> {
    val views = mutableListOf<NearMissRecordView>()
    for (item in items) {
        views.add(toNearMissRecordView(item))
    }
    return views
}

fun sumNearMissRecordView(view: NearMissRecordView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDurationMinutes.toDouble() + parsedCeiling
}
