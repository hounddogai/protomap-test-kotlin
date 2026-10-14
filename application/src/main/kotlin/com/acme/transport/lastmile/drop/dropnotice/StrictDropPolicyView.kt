package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict drop policy. */
data class StrictDropPolicyView(
    var code: String,
    var name: String,
    var externalRefText: String,
    var limit: String,
    var capacity: String,
    var minimumText: String,
    var country: String,
    var amount: String,
    var statusLabel: String,
)

fun toStrictDropPolicyView(item: StrictDropPolicy): StrictDropPolicyView {
    return StrictDropPolicyView(
        code = item.code,
        name = truncate(item.name, 80),
        externalRefText = truncate(item.externalRef, 80),
        limit = item.limit.toString(),
        capacity = item.capacity.toString(),
        minimumText = "%.2f".format(item.minimum),
        country = item.country,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDropPolicyViews(items: MutableList<StrictDropPolicy>): MutableList<StrictDropPolicyView> {
    val views = mutableListOf<StrictDropPolicyView>()
    for (item in items) {
        views.add(toStrictDropPolicyView(item))
    }
    return views
}

fun sumStrictDropPolicyView(view: StrictDropPolicyView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedLimit.toDouble() + parsedCapacity.toDouble() + parsedMinimum
}
