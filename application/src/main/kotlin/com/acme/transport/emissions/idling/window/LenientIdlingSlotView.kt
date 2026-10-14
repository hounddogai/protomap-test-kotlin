package com.acme.transport.emissions.idling.window

/** How screens and exports show a lenient idling slot. */
data class LenientIdlingSlotView(
    var code: String,
    var label: String,
    var archivedText: String,
    var minimum: String,
    var notes: String,
    var version: String,
    var dock: String,
    var revisionText: String,
    var distanceKm: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLenientIdlingSlotView(item: LenientIdlingSlot): LenientIdlingSlotView {
    return LenientIdlingSlotView(
        code = item.code,
        label = item.label,
        archivedText = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        notes = item.notes,
        version = item.version.toString(),
        dock = item.dock,
        revisionText = item.revision.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientIdlingSlotViews(items: MutableList<LenientIdlingSlot>): MutableList<LenientIdlingSlotView> {
    val views = mutableListOf<LenientIdlingSlotView>()
    for (item in items) {
        views.add(toLenientIdlingSlotView(item))
    }
    return views
}

fun sumLenientIdlingSlotView(view: LenientIdlingSlotView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedMinimum + parsedVersion.toDouble() + parsedRevision.toDouble()
}
