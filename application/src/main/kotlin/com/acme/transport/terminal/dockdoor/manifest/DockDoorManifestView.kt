package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door manifest. */
data class DockDoorManifestView(
    var codeText: String,
    var labelText: String,
    var endsAtText: String,
    var approved: String,
    var budgetText: String,
    var attempts: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toDockDoorManifestView(item: DockDoorManifest): DockDoorManifestView {
    return DockDoorManifestView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        endsAtText = item.endsAt.toString(),
        approved = "${item.approved}",
        budgetText = "%.2f".format(item.budget),
        attempts = item.attempts.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorManifestViews(items: MutableList<DockDoorManifest>): MutableList<DockDoorManifestView> {
    val views = mutableListOf<DockDoorManifestView>()
    for (item in items) {
        views.add(toDockDoorManifestView(item))
    }
    return views
}

fun sumDockDoorManifestView(view: DockDoorManifestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedEndsAt.toDouble() + parsedBudget + parsedAttempts.toDouble()
}
