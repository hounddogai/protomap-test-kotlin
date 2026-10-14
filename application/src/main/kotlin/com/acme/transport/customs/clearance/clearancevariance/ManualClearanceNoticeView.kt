package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual clearance notice. */
data class ManualClearanceNoticeView(
    var codeText: String,
    var label: String,
    var sourceText: String,
    var sequence: String,
    var versionText: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toManualClearanceNoticeView(item: ManualClearanceNotice): ManualClearanceNoticeView {
    return ManualClearanceNoticeView(
        codeText = item.code,
        label = truncate(item.label, 80),
        sourceText = item.source,
        sequence = item.sequence.toString(),
        versionText = item.version.toString(),
        vehicleId = truncate(item.vehicleId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearanceNoticeViews(items: MutableList<ManualClearanceNotice>): MutableList<ManualClearanceNoticeView> {
    val views = mutableListOf<ManualClearanceNoticeView>()
    for (item in items) {
        views.add(toManualClearanceNoticeView(item))
    }
    return views
}

fun sumManualClearanceNoticeView(view: ManualClearanceNoticeView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedSequence.toDouble() + parsedVersion.toDouble()
}
