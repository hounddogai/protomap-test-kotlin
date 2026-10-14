package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling slot. */
data class IdlingSlotView(
    var codeText: String,
    var title: String,
    var visibleText: String,
    var threshold: String,
    var unit: String,
    var contactEmailText: String,
    var versionText: String,
    var depotText: String,
    var shiftCode: String,
    var score: String,
    var statusLabel: String,
)

fun toIdlingSlotView(item: IdlingSlot): IdlingSlotView {
    return IdlingSlotView(
        codeText = truncate(item.code, 64),
        title = item.title,
        visibleText = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        unit = truncate(item.unit, 40),
        contactEmailText = truncate(item.contactEmail, 40),
        versionText = item.version.toString(),
        depotText = item.depot,
        shiftCode = item.shiftCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingSlotViews(items: MutableList<IdlingSlot>): MutableList<IdlingSlotView> {
    val views = mutableListOf<IdlingSlotView>()
    for (item in items) {
        views.add(toIdlingSlotView(item))
    }
    return views
}

fun sumIdlingSlotView(view: IdlingSlotView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedVersion = view.versionText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedThreshold + parsedVersion.toDouble() + parsedScore
}
