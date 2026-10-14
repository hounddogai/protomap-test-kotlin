package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary carbon report template. */
data class PrimaryCarbonReportTemplateView(
    var code: String,
    var titleText: String,
    var visible: String,
    var discount: String,
    var localeText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toPrimaryCarbonReportTemplateView(item: PrimaryCarbonReportTemplate): PrimaryCarbonReportTemplateView {
    return PrimaryCarbonReportTemplateView(
        code = truncate(item.code, 40),
        titleText = item.title,
        visible = "${item.visible}",
        discount = "%.2f".format(item.discount),
        localeText = item.locale,
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCarbonReportTemplateViews(items: MutableList<PrimaryCarbonReportTemplate>): MutableList<PrimaryCarbonReportTemplateView> {
    val views = mutableListOf<PrimaryCarbonReportTemplateView>()
    for (item in items) {
        views.add(toPrimaryCarbonReportTemplateView(item))
    }
    return views
}

fun sumPrimaryCarbonReportTemplateView(view: PrimaryCarbonReportTemplateView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
