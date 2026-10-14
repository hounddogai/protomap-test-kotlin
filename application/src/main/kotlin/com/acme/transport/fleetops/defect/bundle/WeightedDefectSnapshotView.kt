package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted defect snapshot. */
data class WeightedDefectSnapshotView(
    var codeText: String,
    var label: String,
    var country: String,
    var checksum: String,
    var loadKgText: String,
    var name: String,
    var retries: String,
    var statusLabel: String,
)

fun toWeightedDefectSnapshotView(item: WeightedDefectSnapshot): WeightedDefectSnapshotView {
    return WeightedDefectSnapshotView(
        codeText = item.code,
        label = truncate(item.label, 40),
        country = truncate(item.country, 40),
        checksum = truncate(item.checksum, 120),
        loadKgText = "%.2f".format(item.loadKg),
        name = truncate(item.name, 80),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDefectSnapshotViews(items: MutableList<WeightedDefectSnapshot>): MutableList<WeightedDefectSnapshotView> {
    val views = mutableListOf<WeightedDefectSnapshotView>()
    for (item in items) {
        views.add(toWeightedDefectSnapshotView(item))
    }
    return views
}

fun sumWeightedDefectSnapshotView(view: WeightedDefectSnapshotView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedLoadKg + parsedRetries.toDouble()
}
