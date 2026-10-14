package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient parcel policy. */
data class LenientParcelPolicyView(
    var code: String,
    var nameText: String,
    var archivedText: String,
    var fuelLitersText: String,
    var category: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toLenientParcelPolicyView(item: LenientParcelPolicy): LenientParcelPolicyView {
    return LenientParcelPolicyView(
        code = truncate(item.code, 80),
        nameText = item.name,
        archivedText = "${item.archived}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        category = item.category,
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientParcelPolicyViews(items: MutableList<LenientParcelPolicy>): MutableList<LenientParcelPolicyView> {
    val views = mutableListOf<LenientParcelPolicyView>()
    for (item in items) {
        views.add(toLenientParcelPolicyView(item))
    }
    return views
}

fun sumLenientParcelPolicyView(view: LenientParcelPolicyView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedFuelLiters + parsedMaximum
}
