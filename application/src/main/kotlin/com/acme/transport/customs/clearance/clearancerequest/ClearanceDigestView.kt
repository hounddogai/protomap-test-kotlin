package com.acme.transport.customs.clearance.clearancerequest

/** How screens and exports show a clearance digest. */
data class ClearanceDigestView(
    var codeText: String,
    var title: String,
    var startsAtText: String,
    var minimum: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toClearanceDigestView(item: ClearanceDigest): ClearanceDigestView {
    return ClearanceDigestView(
        codeText = item.code,
        title = item.title,
        startsAtText = item.startsAt.toString(),
        minimum = "%.2f".format(item.minimum),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceDigestViews(items: MutableList<ClearanceDigest>): MutableList<ClearanceDigestView> {
    val views = mutableListOf<ClearanceDigestView>()
    for (item in items) {
        views.add(toClearanceDigestView(item))
    }
    return views
}

fun sumClearanceDigestView(view: ClearanceDigestView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedStartsAt.toDouble() + parsedMinimum
}
