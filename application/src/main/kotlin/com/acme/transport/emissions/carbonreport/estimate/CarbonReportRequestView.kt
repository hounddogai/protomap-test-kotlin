package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report request. */
data class CarbonReportRequestView(
    var code: String,
    var title: String,
    var checksum: String,
    var loadKg: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toCarbonReportRequestView(item: CarbonReportRequest): CarbonReportRequestView {
    return CarbonReportRequestView(
        code = item.code,
        title = truncate(item.title, 80),
        checksum = item.checksum,
        loadKg = "%.2f".format(item.loadKg),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportRequestViews(items: MutableList<CarbonReportRequest>): MutableList<CarbonReportRequestView> {
    val views = mutableListOf<CarbonReportRequestView>()
    for (item in items) {
        views.add(toCarbonReportRequestView(item))
    }
    return views
}

fun sumCarbonReportRequestView(view: CarbonReportRequestView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedLoadKg + parsedUpdatedAt.toDouble()
}
