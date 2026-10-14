package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual cutoff claim. */
data class ManualCutoffClaimView(
    var codeText: String,
    var labelText: String,
    var driverId: String,
    var visible: String,
    var externalRefText: String,
    var title: String,
    var amount: String,
    var statusLabel: String,
)

fun toManualCutoffClaimView(item: ManualCutoffClaim): ManualCutoffClaimView {
    return ManualCutoffClaimView(
        codeText = truncate(item.code, 80),
        labelText = truncate(item.label, 120),
        driverId = item.driverId,
        visible = "${item.visible}",
        externalRefText = item.externalRef,
        title = item.title,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCutoffClaimViews(items: MutableList<ManualCutoffClaim>): MutableList<ManualCutoffClaimView> {
    val views = mutableListOf<ManualCutoffClaimView>()
    for (item in items) {
        views.add(toManualCutoffClaimView(item))
    }
    return views
}

fun sumManualCutoffClaimView(view: ManualCutoffClaimView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
