package com.acme.transport.crewing.swap.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal swap segment. */
data class SeasonalSwapSegmentView(
    var code: String,
    var nameText: String,
    var pinned: String,
    var checksumText: String,
    var shiftCodeText: String,
    var timezone: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toSeasonalSwapSegmentView(item: SeasonalSwapSegment): SeasonalSwapSegmentView {
    return SeasonalSwapSegmentView(
        code = truncate(item.code, 120),
        nameText = item.name,
        pinned = "${item.pinned}",
        checksumText = truncate(item.checksum, 40),
        shiftCodeText = truncate(item.shiftCode, 64),
        timezone = item.timezone,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalSwapSegmentViews(items: MutableList<SeasonalSwapSegment>): MutableList<SeasonalSwapSegmentView> {
    val views = mutableListOf<SeasonalSwapSegmentView>()
    for (item in items) {
        views.add(toSeasonalSwapSegmentView(item))
    }
    return views
}

fun sumSeasonalSwapSegmentView(view: SeasonalSwapSegmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
