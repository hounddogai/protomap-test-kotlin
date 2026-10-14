package com.acme.transport.safety.nearmiss.slot

/** How screens and exports show a near miss group. */
data class NearMissGroupView(
    var codeText: String,
    var title: String,
    var maximum: String,
    var approvedText: String,
    var pinned: String,
    var etaMinutes: String,
    var routeCode: String,
    var descriptionText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toNearMissGroupView(item: NearMissGroup): NearMissGroupView {
    return NearMissGroupView(
        codeText = item.code,
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        approvedText = "${item.approved}",
        pinned = "${item.pinned}",
        etaMinutes = item.etaMinutes.toString(),
        routeCode = item.routeCode,
        descriptionText = item.description,
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissGroupViews(items: MutableList<NearMissGroup>): MutableList<NearMissGroupView> {
    val views = mutableListOf<NearMissGroupView>()
    for (item in items) {
        views.add(toNearMissGroupView(item))
    }
    return views
}

fun sumNearMissGroupView(view: NearMissGroupView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedMaximum + parsedEtaMinutes.toDouble() + parsedWeight
}
