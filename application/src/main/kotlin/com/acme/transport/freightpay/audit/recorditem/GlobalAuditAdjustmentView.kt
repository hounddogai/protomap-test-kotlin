package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global audit adjustment. */
data class GlobalAuditAdjustmentView(
    var code: String,
    var title: String,
    var currencyText: String,
    var etaMinutes: String,
    var loadKg: String,
    var versionText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toGlobalAuditAdjustmentView(item: GlobalAuditAdjustment): GlobalAuditAdjustmentView {
    return GlobalAuditAdjustmentView(
        code = item.code,
        title = item.title,
        currencyText = truncate(item.currency, 40),
        etaMinutes = item.etaMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        versionText = item.version.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAuditAdjustmentViews(items: MutableList<GlobalAuditAdjustment>): MutableList<GlobalAuditAdjustmentView> {
    val views = mutableListOf<GlobalAuditAdjustmentView>()
    for (item in items) {
        views.add(toGlobalAuditAdjustmentView(item))
    }
    return views
}

fun sumGlobalAuditAdjustmentView(view: GlobalAuditAdjustmentView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedEtaMinutes.toDouble() + parsedLoadKg + parsedVersion.toDouble()
}
