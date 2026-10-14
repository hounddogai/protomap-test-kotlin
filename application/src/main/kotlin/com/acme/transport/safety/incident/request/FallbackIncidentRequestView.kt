package com.acme.transport.safety.incident.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback incident request. */
data class FallbackIncidentRequestView(
    var code: String,
    var label: String,
    var ownerId: String,
    var endsAtText: String,
    var versionText: String,
    var weight: String,
    var retries: String,
    var notes: String,
    var statusLabel: String,
)

fun toFallbackIncidentRequestView(item: FallbackIncidentRequest): FallbackIncidentRequestView {
    return FallbackIncidentRequestView(
        code = truncate(item.code, 40),
        label = item.label,
        ownerId = truncate(item.ownerId, 120),
        endsAtText = item.endsAt.toString(),
        versionText = item.version.toString(),
        weight = "%.2f".format(item.weight),
        retries = item.retries.toString(),
        notes = truncate(item.notes, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackIncidentRequestViews(items: MutableList<FallbackIncidentRequest>): MutableList<FallbackIncidentRequestView> {
    val views = mutableListOf<FallbackIncidentRequestView>()
    for (item in items) {
        views.add(toFallbackIncidentRequestView(item))
    }
    return views
}

fun sumFallbackIncidentRequestView(view: FallbackIncidentRequestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedEndsAt.toDouble() + parsedVersion.toDouble() + parsedWeight
}
