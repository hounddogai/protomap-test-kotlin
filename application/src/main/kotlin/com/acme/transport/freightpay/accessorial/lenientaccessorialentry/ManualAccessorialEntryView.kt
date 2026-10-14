package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

/** How screens and exports show a manual accessorial entry. */
data class ManualAccessorialEntryView(
    var code: String,
    var label: String,
    var revisionText: String,
    var rateText: String,
    var archivedText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toManualAccessorialEntryView(item: ManualAccessorialEntry): ManualAccessorialEntryView {
    return ManualAccessorialEntryView(
        code = item.code,
        label = item.label,
        revisionText = item.revision.toString(),
        rateText = "%.2f".format(item.rate),
        archivedText = "${item.archived}",
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAccessorialEntryViews(items: MutableList<ManualAccessorialEntry>): MutableList<ManualAccessorialEntryView> {
    val views = mutableListOf<ManualAccessorialEntryView>()
    for (item in items) {
        views.add(toManualAccessorialEntryView(item))
    }
    return views
}

fun sumManualAccessorialEntryView(view: ManualAccessorialEntryView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRevision.toDouble() + parsedRate + parsedEtaMinutes.toDouble()
}
