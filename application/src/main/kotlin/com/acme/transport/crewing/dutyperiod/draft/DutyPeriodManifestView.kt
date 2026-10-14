package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period manifest. */
data class DutyPeriodManifestView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var segment: String,
    var carrier: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDutyPeriodManifestView(item: DutyPeriodManifest): DutyPeriodManifestView {
    return DutyPeriodManifestView(
        code = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        segment = item.segment,
        carrier = truncate(item.carrier, 80),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodManifestViews(items: MutableList<DutyPeriodManifest>): MutableList<DutyPeriodManifestView> {
    val views = mutableListOf<DutyPeriodManifestView>()
    for (item in items) {
        views.add(toDutyPeriodManifestView(item))
    }
    return views
}

fun sumDutyPeriodManifestView(view: DutyPeriodManifestView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
