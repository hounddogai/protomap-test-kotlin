package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

/** How screens and exports show a manual proof of delivery record. */
data class ManualProofOfDeliveryRecordView(
    var codeText: String,
    var title: String,
    var active: String,
    var source: String,
    var segment: String,
    var notes: String,
    var version: String,
    var statusLabel: String,
)

fun toManualProofOfDeliveryRecordView(item: ManualProofOfDeliveryRecord): ManualProofOfDeliveryRecordView {
    return ManualProofOfDeliveryRecordView(
        codeText = item.code,
        title = item.title,
        active = "${item.active}",
        source = item.source,
        segment = item.segment,
        notes = item.notes,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualProofOfDeliveryRecordViews(items: MutableList<ManualProofOfDeliveryRecord>): MutableList<ManualProofOfDeliveryRecordView> {
    val views = mutableListOf<ManualProofOfDeliveryRecordView>()
    for (item in items) {
        views.add(toManualProofOfDeliveryRecordView(item))
    }
    return views
}

fun sumManualProofOfDeliveryRecordView(view: ManualProofOfDeliveryRecordView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
