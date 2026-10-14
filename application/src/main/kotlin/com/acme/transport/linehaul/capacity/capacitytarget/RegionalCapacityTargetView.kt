package com.acme.transport.linehaul.capacity.capacitytarget

/** How screens and exports show a regional capacity target. */
data class RegionalCapacityTargetView(
    var codeText: String,
    var name: String,
    var archived: String,
    var unitText: String,
    var shiftCodeText: String,
    var minimum: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toRegionalCapacityTargetView(item: RegionalCapacityTarget): RegionalCapacityTargetView {
    return RegionalCapacityTargetView(
        codeText = item.code,
        name = item.name,
        archived = "${item.archived}",
        unitText = item.unit,
        shiftCodeText = item.shiftCode,
        minimum = "%.2f".format(item.minimum),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityTargetViews(items: MutableList<RegionalCapacityTarget>): MutableList<RegionalCapacityTargetView> {
    val views = mutableListOf<RegionalCapacityTargetView>()
    for (item in items) {
        views.add(toRegionalCapacityTargetView(item))
    }
    return views
}

fun sumRegionalCapacityTargetView(view: RegionalCapacityTargetView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedMinimum + parsedLoadKg
}
