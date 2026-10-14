package com.acme.transport.crewing.swap.bundle

/** How screens and exports show a capped swap policy. */
data class CappedSwapPolicyView(
    var code: String,
    var titleText: String,
    var contactEmail: String,
    var loadKg: String,
    var windowDays: String,
    var timezoneText: String,
    var name: String,
    var floorValue: String,
    var startsAt: String,
    var version: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toCappedSwapPolicyView(item: CappedSwapPolicy): CappedSwapPolicyView {
    return CappedSwapPolicyView(
        code = item.code,
        titleText = item.title,
        contactEmail = item.contactEmail,
        loadKg = "%.2f".format(item.loadKg),
        windowDays = item.windowDays.toString(),
        timezoneText = item.timezone,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        startsAt = item.startsAt.toString(),
        version = item.version.toString(),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedSwapPolicyViews(items: MutableList<CappedSwapPolicy>): MutableList<CappedSwapPolicyView> {
    val views = mutableListOf<CappedSwapPolicyView>()
    for (item in items) {
        views.add(toCappedSwapPolicyView(item))
    }
    return views
}

fun sumCappedSwapPolicyView(view: CappedSwapPolicyView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedLoadKg + parsedWindowDays.toDouble() + parsedFloorValue
}
