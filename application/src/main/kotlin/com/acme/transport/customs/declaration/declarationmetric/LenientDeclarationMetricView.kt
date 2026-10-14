package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient declaration metric. */
data class LenientDeclarationMetricView(
    var codeText: String,
    var title: String,
    var vehicleIdText: String,
    var checksumText: String,
    var country: String,
    var shiftCodeText: String,
    var createdAt: String,
    var timezoneText: String,
    var version: String,
    var statusLabel: String,
)

fun toLenientDeclarationMetricView(item: LenientDeclarationMetric): LenientDeclarationMetricView {
    return LenientDeclarationMetricView(
        codeText = item.code,
        title = item.title,
        vehicleIdText = item.vehicleId,
        checksumText = item.checksum,
        country = truncate(item.country, 80),
        shiftCodeText = item.shiftCode,
        createdAt = item.createdAt.toString(),
        timezoneText = truncate(item.timezone, 80),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDeclarationMetricViews(items: MutableList<LenientDeclarationMetric>): MutableList<LenientDeclarationMetricView> {
    val views = mutableListOf<LenientDeclarationMetricView>()
    for (item in items) {
        views.add(toLenientDeclarationMetricView(item))
    }
    return views
}

fun sumLenientDeclarationMetricView(view: LenientDeclarationMetricView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedVersion = view.version.toInt()
    return parsedCreatedAt.toDouble() + parsedVersion.toDouble()
}
