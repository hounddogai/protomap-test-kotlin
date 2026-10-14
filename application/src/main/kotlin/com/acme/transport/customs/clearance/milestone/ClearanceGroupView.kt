package com.acme.transport.customs.clearance.milestone

/** How screens and exports show a clearance group. */
data class ClearanceGroupView(
    var codeText: String,
    var name: String,
    var discount: String,
    var descriptionText: String,
    var windowDays: String,
    var externalRef: String,
    var score: String,
    var statusLabel: String,
)

fun toClearanceGroupView(item: ClearanceGroup): ClearanceGroupView {
    return ClearanceGroupView(
        codeText = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        descriptionText = item.description,
        windowDays = item.windowDays.toString(),
        externalRef = item.externalRef,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceGroupViews(items: MutableList<ClearanceGroup>): MutableList<ClearanceGroupView> {
    val views = mutableListOf<ClearanceGroupView>()
    for (item in items) {
        views.add(toClearanceGroupView(item))
    }
    return views
}

fun sumClearanceGroupView(view: ClearanceGroupView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.score.toDouble()
    return parsedDiscount + parsedWindowDays.toDouble() + parsedScore
}
