package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker record. */
data class LockerRecordView(
    var codeText: String,
    var nameText: String,
    var externalRef: String,
    var discount: String,
    var etaMinutes: String,
    var graceDays: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toLockerRecordView(item: LockerRecord): LockerRecordView {
    return LockerRecordView(
        codeText = item.code,
        nameText = item.name,
        externalRef = truncate(item.externalRef, 40),
        discount = "%.2f".format(item.discount),
        etaMinutes = item.etaMinutes.toString(),
        graceDays = item.graceDays.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerRecordViews(items: MutableList<LockerRecord>): MutableList<LockerRecordView> {
    val views = mutableListOf<LockerRecordView>()
    for (item in items) {
        views.add(toLockerRecordView(item))
    }
    return views
}

fun sumLockerRecordView(view: LockerRecordView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDiscount + parsedEtaMinutes.toDouble() + parsedGraceDays.toDouble()
}
