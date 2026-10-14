package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global parcel assignment. */
data class GlobalParcelAssignmentView(
    var codeText: String,
    var name: String,
    var distanceKm: String,
    var loadKgText: String,
    var endsAtText: String,
    var currencyText: String,
    var ceilingText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toGlobalParcelAssignmentView(item: GlobalParcelAssignment): GlobalParcelAssignmentView {
    return GlobalParcelAssignmentView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        loadKgText = "%.2f".format(item.loadKg),
        endsAtText = item.endsAt.toString(),
        currencyText = truncate(item.currency, 40),
        ceilingText = "%.2f".format(item.ceiling),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalParcelAssignmentViews(items: MutableList<GlobalParcelAssignment>): MutableList<GlobalParcelAssignmentView> {
    val views = mutableListOf<GlobalParcelAssignmentView>()
    for (item in items) {
        views.add(toGlobalParcelAssignmentView(item))
    }
    return views
}

fun sumGlobalParcelAssignmentView(view: GlobalParcelAssignmentView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDistanceKm + parsedLoadKg + parsedEndsAt.toDouble()
}
