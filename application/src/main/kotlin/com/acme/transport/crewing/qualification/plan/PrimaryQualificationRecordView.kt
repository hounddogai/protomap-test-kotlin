package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary qualification record. */
data class PrimaryQualificationRecordView(
    var codeText: String,
    var label: String,
    var region: String,
    var retriesText: String,
    var approvedText: String,
    var segment: String,
    var marginText: String,
    var dock: String,
    var driverId: String,
    var visible: String,
    var statusLabel: String,
)

fun toPrimaryQualificationRecordView(item: PrimaryQualificationRecord): PrimaryQualificationRecordView {
    return PrimaryQualificationRecordView(
        codeText = item.code,
        label = truncate(item.label, 64),
        region = item.region,
        retriesText = item.retries.toString(),
        approvedText = "${item.approved}",
        segment = item.segment,
        marginText = "%.2f".format(item.margin),
        dock = item.dock,
        driverId = item.driverId,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryQualificationRecordViews(items: MutableList<PrimaryQualificationRecord>): MutableList<PrimaryQualificationRecordView> {
    val views = mutableListOf<PrimaryQualificationRecordView>()
    for (item in items) {
        views.add(toPrimaryQualificationRecordView(item))
    }
    return views
}

fun sumPrimaryQualificationRecordView(view: PrimaryQualificationRecordView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedRetries.toDouble() + parsedMargin
}
