package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tariff draft. */
data class RollingTariffDraftView(
    var codeText: String,
    var name: String,
    var versionText: String,
    var floorValue: String,
    var checksumText: String,
    var labelText: String,
    var rate: String,
    var etaMinutes: String,
    var contactEmailText: String,
    var region: String,
    var statusLabel: String,
)

fun toRollingTariffDraftView(item: RollingTariffDraft): RollingTariffDraftView {
    return RollingTariffDraftView(
        codeText = item.code,
        name = item.name,
        versionText = item.version.toString(),
        floorValue = "%.2f".format(item.floorValue),
        checksumText = item.checksum,
        labelText = item.label,
        rate = "%.2f".format(item.rate),
        etaMinutes = item.etaMinutes.toString(),
        contactEmailText = item.contactEmail,
        region = truncate(item.region, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTariffDraftViews(items: MutableList<RollingTariffDraft>): MutableList<RollingTariffDraftView> {
    val views = mutableListOf<RollingTariffDraftView>()
    for (item in items) {
        views.add(toRollingTariffDraftView(item))
    }
    return views
}

fun sumRollingTariffDraftView(view: RollingTariffDraftView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedVersion.toDouble() + parsedFloorValue + parsedRate
}
