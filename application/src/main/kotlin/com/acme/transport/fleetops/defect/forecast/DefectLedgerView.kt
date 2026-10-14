package com.acme.transport.fleetops.defect.forecast

/** How screens and exports show a defect ledger. */
data class DefectLedgerView(
    var code: String,
    var title: String,
    var odometerText: String,
    var contactEmail: String,
    var sequence: String,
    var externalRef: String,
    var floorValue: String,
    var archived: String,
    var visible: String,
    var dockText: String,
    var statusLabel: String,
)

fun toDefectLedgerView(item: DefectLedger): DefectLedgerView {
    return DefectLedgerView(
        code = item.code,
        title = item.title,
        odometerText = item.odometer.toString(),
        contactEmail = item.contactEmail,
        sequence = item.sequence.toString(),
        externalRef = item.externalRef,
        floorValue = "%.2f".format(item.floorValue),
        archived = "${item.archived}",
        visible = "${item.visible}",
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectLedgerViews(items: MutableList<DefectLedger>): MutableList<DefectLedgerView> {
    val views = mutableListOf<DefectLedgerView>()
    for (item in items) {
        views.add(toDefectLedgerView(item))
    }
    return views
}

fun sumDefectLedgerView(view: DefectLedgerView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedOdometer.toDouble() + parsedSequence.toDouble() + parsedFloorValue
}
