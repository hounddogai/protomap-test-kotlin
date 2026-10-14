package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual near miss profile. */
data class ManualNearMissProfileView(
    var code: String,
    var name: String,
    var checksum: String,
    var externalRef: String,
    var unitText: String,
    var updatedAt: String,
    var archived: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toManualNearMissProfileView(item: ManualNearMissProfile): ManualNearMissProfileView {
    return ManualNearMissProfileView(
        code = item.code,
        name = truncate(item.name, 120),
        checksum = item.checksum,
        externalRef = truncate(item.externalRef, 40),
        unitText = item.unit,
        updatedAt = item.updatedAt.toString(),
        archived = "${item.archived}",
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualNearMissProfileViews(items: MutableList<ManualNearMissProfile>): MutableList<ManualNearMissProfileView> {
    val views = mutableListOf<ManualNearMissProfileView>()
    for (item in items) {
        views.add(toManualNearMissProfileView(item))
    }
    return views
}

fun sumManualNearMissProfileView(view: ManualNearMissProfileView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedLoadKg
}
