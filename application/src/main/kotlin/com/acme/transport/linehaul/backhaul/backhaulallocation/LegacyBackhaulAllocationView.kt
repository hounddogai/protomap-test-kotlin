package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy backhaul allocation. */
data class LegacyBackhaulAllocationView(
    var code: String,
    var nameText: String,
    var score: String,
    var loadKgText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toLegacyBackhaulAllocationView(item: LegacyBackhaulAllocation): LegacyBackhaulAllocationView {
    return LegacyBackhaulAllocationView(
        code = item.code,
        nameText = truncate(item.name, 80),
        score = "%.2f".format(item.score),
        loadKgText = "%.2f".format(item.loadKg),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBackhaulAllocationViews(items: MutableList<LegacyBackhaulAllocation>): MutableList<LegacyBackhaulAllocationView> {
    val views = mutableListOf<LegacyBackhaulAllocationView>()
    for (item in items) {
        views.add(toLegacyBackhaulAllocationView(item))
    }
    return views
}

fun sumLegacyBackhaulAllocationView(view: LegacyBackhaulAllocationView): Double {
    val parsedScore = view.score.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedScore + parsedLoadKg
}
