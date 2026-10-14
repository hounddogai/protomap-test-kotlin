package com.acme.transport.terminal.dwell.dwellcycle

/** How screens and exports show a regional dwell digest. */
data class RegionalDwellDigestView(
    var code: String,
    var label: String,
    var reviewer: String,
    var amount: String,
    var active: String,
    var statusLabel: String,
)

fun toRegionalDwellDigestView(item: RegionalDwellDigest): RegionalDwellDigestView {
    return RegionalDwellDigestView(
        code = item.code,
        label = item.label,
        reviewer = item.reviewer,
        amount = "%.2f".format(item.amount),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDwellDigestViews(items: MutableList<RegionalDwellDigest>): MutableList<RegionalDwellDigestView> {
    val views = mutableListOf<RegionalDwellDigestView>()
    for (item in items) {
        views.add(toRegionalDwellDigestView(item))
    }
    return views
}

fun sumRegionalDwellDigestView(view: RegionalDwellDigestView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
