package com.acme.transport.terminal.dwell.rule

/** How screens and exports show a regional dwell profile. */
data class RegionalDwellProfileView(
    var code: String,
    var title: String,
    var approved: String,
    var discount: String,
    var maximum: String,
    var segment: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toRegionalDwellProfileView(item: RegionalDwellProfile): RegionalDwellProfileView {
    return RegionalDwellProfileView(
        code = item.code,
        title = item.title,
        approved = "${item.approved}",
        discount = "%.2f".format(item.discount),
        maximum = "%.2f".format(item.maximum),
        segment = item.segment,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDwellProfileViews(items: MutableList<RegionalDwellProfile>): MutableList<RegionalDwellProfileView> {
    val views = mutableListOf<RegionalDwellProfileView>()
    for (item in items) {
        views.add(toRegionalDwellProfileView(item))
    }
    return views
}

fun sumRegionalDwellProfileView(view: RegionalDwellProfileView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDiscount + parsedMaximum + parsedGraceDays.toDouble()
}
