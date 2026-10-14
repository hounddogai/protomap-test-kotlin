package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event audit. */
data class CameraEventAuditView(
    var codeText: String,
    var label: String,
    var sourceText: String,
    var title: String,
    var visible: String,
    var vehicleId: String,
    var carrier: String,
    var segment: String,
    var currency: String,
    var discount: String,
    var statusLabel: String,
)

fun toCameraEventAuditView(item: CameraEventAudit): CameraEventAuditView {
    return CameraEventAuditView(
        codeText = truncate(item.code, 40),
        label = item.label,
        sourceText = item.source,
        title = truncate(item.title, 80),
        visible = "${item.visible}",
        vehicleId = item.vehicleId,
        carrier = item.carrier,
        segment = item.segment,
        currency = item.currency,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventAuditViews(items: MutableList<CameraEventAudit>): MutableList<CameraEventAuditView> {
    val views = mutableListOf<CameraEventAuditView>()
    for (item in items) {
        views.add(toCameraEventAuditView(item))
    }
    return views
}

fun sumCameraEventAuditView(view: CameraEventAuditView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
