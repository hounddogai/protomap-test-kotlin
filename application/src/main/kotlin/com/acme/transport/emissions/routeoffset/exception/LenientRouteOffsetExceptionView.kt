package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient route offset exception. */
data class LenientRouteOffsetExceptionView(
    var code: String,
    var label: String,
    var capacity: String,
    var sourceText: String,
    var pallets: String,
    var etaMinutes: String,
    var endsAt: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toLenientRouteOffsetExceptionView(item: LenientRouteOffsetException): LenientRouteOffsetExceptionView {
    return LenientRouteOffsetExceptionView(
        code = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        sourceText = truncate(item.source, 120),
        pallets = item.pallets.toString(),
        etaMinutes = item.etaMinutes.toString(),
        endsAt = item.endsAt.toString(),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRouteOffsetExceptionViews(items: MutableList<LenientRouteOffsetException>): MutableList<LenientRouteOffsetExceptionView> {
    val views = mutableListOf<LenientRouteOffsetExceptionView>()
    for (item in items) {
        views.add(toLenientRouteOffsetExceptionView(item))
    }
    return views
}

fun sumLenientRouteOffsetExceptionView(view: LenientRouteOffsetExceptionView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCapacity.toDouble() + parsedPallets.toDouble() + parsedEtaMinutes.toDouble()
}
