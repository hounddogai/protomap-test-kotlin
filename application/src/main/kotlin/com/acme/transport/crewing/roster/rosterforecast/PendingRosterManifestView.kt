package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending roster manifest. */
data class PendingRosterManifestView(
    var code: String,
    var label: String,
    var ownerId: String,
    var sequenceText: String,
    var contactEmail: String,
    var priorityText: String,
    var externalRef: String,
    var weight: String,
    var dock: String,
    var vehicleId: String,
    var retries: String,
    var statusLabel: String,
)

fun toPendingRosterManifestView(item: PendingRosterManifest): PendingRosterManifestView {
    return PendingRosterManifestView(
        code = item.code,
        label = truncate(item.label, 40),
        ownerId = truncate(item.ownerId, 40),
        sequenceText = item.sequence.toString(),
        contactEmail = item.contactEmail,
        priorityText = item.priority.toString(),
        externalRef = truncate(item.externalRef, 120),
        weight = "%.2f".format(item.weight),
        dock = item.dock,
        vehicleId = item.vehicleId,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRosterManifestViews(items: MutableList<PendingRosterManifest>): MutableList<PendingRosterManifestView> {
    val views = mutableListOf<PendingRosterManifestView>()
    for (item in items) {
        views.add(toPendingRosterManifestView(item))
    }
    return views
}

fun sumPendingRosterManifestView(view: PendingRosterManifestView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedSequence.toDouble() + parsedPriority.toDouble() + parsedWeight
}
