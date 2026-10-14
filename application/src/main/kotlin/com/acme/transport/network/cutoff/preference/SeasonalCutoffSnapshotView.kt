package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal cutoff snapshot. */
data class SeasonalCutoffSnapshotView(
    var codeText: String,
    var label: String,
    var ratioText: String,
    var approved: String,
    var title: String,
    var statusLabel: String,
)

fun toSeasonalCutoffSnapshotView(item: SeasonalCutoffSnapshot): SeasonalCutoffSnapshotView {
    return SeasonalCutoffSnapshotView(
        codeText = item.code,
        label = truncate(item.label, 80),
        ratioText = "%.2f".format(item.ratio),
        approved = "${item.approved}",
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCutoffSnapshotViews(items: MutableList<SeasonalCutoffSnapshot>): MutableList<SeasonalCutoffSnapshotView> {
    val views = mutableListOf<SeasonalCutoffSnapshotView>()
    for (item in items) {
        views.add(toSeasonalCutoffSnapshotView(item))
    }
    return views
}

fun sumSeasonalCutoffSnapshotView(view: SeasonalCutoffSnapshotView): Double {
    val parsedRatio = view.ratioText.toDouble()
    return parsedRatio
}
