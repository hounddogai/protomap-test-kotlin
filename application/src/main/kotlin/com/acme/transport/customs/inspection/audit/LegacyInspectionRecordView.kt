package com.acme.transport.customs.inspection.audit

/** How screens and exports show a legacy inspection record. */
data class LegacyInspectionRecordView(
    var codeText: String,
    var labelText: String,
    var etaMinutes: String,
    var shiftCodeText: String,
    var name: String,
    var unitText: String,
    var stopCount: String,
    var revision: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toLegacyInspectionRecordView(item: LegacyInspectionRecord): LegacyInspectionRecordView {
    return LegacyInspectionRecordView(
        codeText = item.code,
        labelText = item.label,
        etaMinutes = item.etaMinutes.toString(),
        shiftCodeText = item.shiftCode,
        name = item.name,
        unitText = item.unit,
        stopCount = item.stopCount.toString(),
        revision = item.revision.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionRecordViews(items: MutableList<LegacyInspectionRecord>): MutableList<LegacyInspectionRecordView> {
    val views = mutableListOf<LegacyInspectionRecordView>()
    for (item in items) {
        views.add(toLegacyInspectionRecordView(item))
    }
    return views
}

fun sumLegacyInspectionRecordView(view: LegacyInspectionRecordView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedEtaMinutes.toDouble() + parsedStopCount.toDouble() + parsedRevision.toDouble()
}
