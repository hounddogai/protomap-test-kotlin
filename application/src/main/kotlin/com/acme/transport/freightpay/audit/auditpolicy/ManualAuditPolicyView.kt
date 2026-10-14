package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual audit policy. */
data class ManualAuditPolicyView(
    var codeText: String,
    var title: String,
    var palletsText: String,
    var versionText: String,
    var trailerId: String,
    var carrier: String,
    var phoneText: String,
    var ratio: String,
    var threshold: String,
    var weightText: String,
    var statusLabel: String,
)

fun toManualAuditPolicyView(item: ManualAuditPolicy): ManualAuditPolicyView {
    return ManualAuditPolicyView(
        codeText = truncate(item.code, 80),
        title = item.title,
        palletsText = item.pallets.toString(),
        versionText = item.version.toString(),
        trailerId = item.trailerId,
        carrier = item.carrier,
        phoneText = item.phone,
        ratio = "%.2f".format(item.ratio),
        threshold = "%.2f".format(item.threshold),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAuditPolicyViews(items: MutableList<ManualAuditPolicy>): MutableList<ManualAuditPolicyView> {
    val views = mutableListOf<ManualAuditPolicyView>()
    for (item in items) {
        views.add(toManualAuditPolicyView(item))
    }
    return views
}

fun sumManualAuditPolicyView(view: ManualAuditPolicyView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedPallets.toDouble() + parsedVersion.toDouble() + parsedRatio
}
