package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary ev charge digest. */
data class PrimaryEvChargeDigestView(
    var code: String,
    var name: String,
    var ceilingText: String,
    var quantityText: String,
    var region: String,
    var versionText: String,
    var visible: String,
    var revision: String,
    var unit: String,
    var statusLabel: String,
)

fun toPrimaryEvChargeDigestView(item: PrimaryEvChargeDigest): PrimaryEvChargeDigestView {
    return PrimaryEvChargeDigestView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 120),
        ceilingText = "%.2f".format(item.ceiling),
        quantityText = item.quantity.toString(),
        region = item.region,
        versionText = item.version.toString(),
        visible = "${item.visible}",
        revision = item.revision.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEvChargeDigestViews(items: MutableList<PrimaryEvChargeDigest>): MutableList<PrimaryEvChargeDigestView> {
    val views = mutableListOf<PrimaryEvChargeDigestView>()
    for (item in items) {
        views.add(toPrimaryEvChargeDigestView(item))
    }
    return views
}

fun sumPrimaryEvChargeDigestView(view: PrimaryEvChargeDigestView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedCeiling + parsedQuantity.toDouble() + parsedVersion.toDouble()
}
