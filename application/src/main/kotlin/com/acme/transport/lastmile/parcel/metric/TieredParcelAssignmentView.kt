package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered parcel assignment. */
data class TieredParcelAssignmentView(
    var code: String,
    var name: String,
    var activeText: String,
    var currency: String,
    var retries: String,
    var statusLabel: String,
)

fun toTieredParcelAssignmentView(item: TieredParcelAssignment): TieredParcelAssignmentView {
    return TieredParcelAssignmentView(
        code = truncate(item.code, 64),
        name = item.name,
        activeText = "${item.active}",
        currency = item.currency,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredParcelAssignmentViews(items: MutableList<TieredParcelAssignment>): MutableList<TieredParcelAssignmentView> {
    val views = mutableListOf<TieredParcelAssignmentView>()
    for (item in items) {
        views.add(toTieredParcelAssignmentView(item))
    }
    return views
}

fun sumTieredParcelAssignmentView(view: TieredParcelAssignmentView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
