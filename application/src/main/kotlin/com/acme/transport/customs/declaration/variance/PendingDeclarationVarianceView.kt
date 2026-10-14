package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending declaration variance. */
data class PendingDeclarationVarianceView(
    var code: String,
    var label: String,
    var threshold: String,
    var archivedText: String,
    var palletsText: String,
    var phone: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toPendingDeclarationVarianceView(item: PendingDeclarationVariance): PendingDeclarationVarianceView {
    return PendingDeclarationVarianceView(
        code = item.code,
        label = truncate(item.label, 80),
        threshold = "%.2f".format(item.threshold),
        archivedText = "${item.archived}",
        palletsText = item.pallets.toString(),
        phone = item.phone,
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDeclarationVarianceViews(items: MutableList<PendingDeclarationVariance>): MutableList<PendingDeclarationVarianceView> {
    val views = mutableListOf<PendingDeclarationVarianceView>()
    for (item in items) {
        views.add(toPendingDeclarationVarianceView(item))
    }
    return views
}

fun sumPendingDeclarationVarianceView(view: PendingDeclarationVarianceView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedThreshold + parsedPallets.toDouble()
}
