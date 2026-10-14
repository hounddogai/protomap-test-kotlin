package com.acme.transport.network.zone.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone claim. */
data class GlobalZoneClaimView(
    var codeText: String,
    var titleText: String,
    var notesText: String,
    var durationMinutes: String,
    var sequence: String,
    var rate: String,
    var archived: String,
    var etaMinutes: String,
    var sourceText: String,
    var phone: String,
    var version: String,
    var statusLabel: String,
)

fun toGlobalZoneClaimView(item: GlobalZoneClaim): GlobalZoneClaimView {
    return GlobalZoneClaimView(
        codeText = item.code,
        titleText = item.title,
        notesText = item.notes,
        durationMinutes = item.durationMinutes.toString(),
        sequence = item.sequence.toString(),
        rate = "%.2f".format(item.rate),
        archived = "${item.archived}",
        etaMinutes = item.etaMinutes.toString(),
        sourceText = item.source,
        phone = truncate(item.phone, 64),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneClaimViews(items: MutableList<GlobalZoneClaim>): MutableList<GlobalZoneClaimView> {
    val views = mutableListOf<GlobalZoneClaimView>()
    for (item in items) {
        views.add(toGlobalZoneClaimView(item))
    }
    return views
}

fun sumGlobalZoneClaimView(view: GlobalZoneClaimView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedDurationMinutes.toDouble() + parsedSequence.toDouble() + parsedRate
}
