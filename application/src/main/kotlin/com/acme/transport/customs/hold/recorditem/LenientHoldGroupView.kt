package com.acme.transport.customs.hold.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient hold group. */
data class LenientHoldGroupView(
    var codeText: String,
    var label: String,
    var versionText: String,
    var country: String,
    var margin: String,
    var ratioText: String,
    var updatedAt: String,
    var unitText: String,
    var statusLabel: String,
)

fun toLenientHoldGroupView(item: LenientHoldGroup): LenientHoldGroupView {
    return LenientHoldGroupView(
        codeText = truncate(item.code, 80),
        label = item.label,
        versionText = item.version.toString(),
        country = item.country,
        margin = "%.2f".format(item.margin),
        ratioText = "%.2f".format(item.ratio),
        updatedAt = item.updatedAt.toString(),
        unitText = truncate(item.unit, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientHoldGroupViews(items: MutableList<LenientHoldGroup>): MutableList<LenientHoldGroupView> {
    val views = mutableListOf<LenientHoldGroupView>()
    for (item in items) {
        views.add(toLenientHoldGroupView(item))
    }
    return views
}

fun sumLenientHoldGroupView(view: LenientHoldGroupView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedVersion.toDouble() + parsedMargin + parsedRatio
}
