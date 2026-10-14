package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual camera event policy. */
data class ManualCameraEventPolicyView(
    var codeText: String,
    var name: String,
    var source: String,
    var ceilingText: String,
    var vehicleId: String,
    var score: String,
    var palletsText: String,
    var minimum: String,
    var archived: String,
    var timezone: String,
    var weightText: String,
    var statusLabel: String,
)

fun toManualCameraEventPolicyView(item: ManualCameraEventPolicy): ManualCameraEventPolicyView {
    return ManualCameraEventPolicyView(
        codeText = item.code,
        name = item.name,
        source = truncate(item.source, 40),
        ceilingText = "%.2f".format(item.ceiling),
        vehicleId = item.vehicleId,
        score = "%.2f".format(item.score),
        palletsText = item.pallets.toString(),
        minimum = "%.2f".format(item.minimum),
        archived = "${item.archived}",
        timezone = item.timezone,
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventPolicyViews(items: MutableList<ManualCameraEventPolicy>): MutableList<ManualCameraEventPolicyView> {
    val views = mutableListOf<ManualCameraEventPolicyView>()
    for (item in items) {
        views.add(toManualCameraEventPolicyView(item))
    }
    return views
}

fun sumManualCameraEventPolicyView(view: ManualCameraEventPolicyView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedCeiling + parsedScore + parsedPallets.toDouble()
}
