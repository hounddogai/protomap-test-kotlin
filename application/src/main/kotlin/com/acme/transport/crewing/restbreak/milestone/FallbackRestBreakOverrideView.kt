package com.acme.transport.crewing.restbreak.milestone

/** How screens and exports show a fallback rest break override. */
data class FallbackRestBreakOverrideView(
    var codeText: String,
    var label: String,
    var archived: String,
    var depot: String,
    var contactEmailText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toFallbackRestBreakOverrideView(item: FallbackRestBreakOverride): FallbackRestBreakOverrideView {
    return FallbackRestBreakOverrideView(
        codeText = item.code,
        label = item.label,
        archived = "${item.archived}",
        depot = item.depot,
        contactEmailText = item.contactEmail,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRestBreakOverrideViews(items: MutableList<FallbackRestBreakOverride>): MutableList<FallbackRestBreakOverrideView> {
    val views = mutableListOf<FallbackRestBreakOverrideView>()
    for (item in items) {
        views.add(toFallbackRestBreakOverrideView(item))
    }
    return views
}

fun sumFallbackRestBreakOverrideView(view: FallbackRestBreakOverrideView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
