package com.acme.transport.terminal.sortation.request

/** How screens and exports show a sortation request. */
data class SortationRequestView(
    var code: String,
    var label: String,
    var reviewer: String,
    var countryText: String,
    var approvedText: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toSortationRequestView(item: SortationRequest): SortationRequestView {
    return SortationRequestView(
        code = item.code,
        label = item.label,
        reviewer = item.reviewer,
        countryText = item.country,
        approvedText = "${item.approved}",
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationRequestViews(items: MutableList<SortationRequest>): MutableList<SortationRequestView> {
    val views = mutableListOf<SortationRequestView>()
    for (item in items) {
        views.add(toSortationRequestView(item))
    }
    return views
}

fun sumSortationRequestView(view: SortationRequestView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}
