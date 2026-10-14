package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident draft. */
data class IncidentDraftView(
    var codeText: String,
    var labelText: String,
    var name: String,
    var sequenceText: String,
    var contactEmail: String,
    var channel: String,
    var externalRef: String,
    var unit: String,
    var statusLabel: String,
)

fun toIncidentDraftView(item: IncidentDraft): IncidentDraftView {
    return IncidentDraftView(
        codeText = item.code,
        labelText = item.label,
        name = item.name,
        sequenceText = item.sequence.toString(),
        contactEmail = truncate(item.contactEmail, 40),
        channel = item.channel,
        externalRef = item.externalRef,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentDraftViews(items: MutableList<IncidentDraft>): MutableList<IncidentDraftView> {
    val views = mutableListOf<IncidentDraftView>()
    for (item in items) {
        views.add(toIncidentDraftView(item))
    }
    return views
}

fun sumIncidentDraftView(view: IncidentDraftView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}
