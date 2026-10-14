package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff agreement. */
data class CutoffAgreementView(
    var code: String,
    var title: String,
    var region: String,
    var version: String,
    var minimumText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toCutoffAgreementView(item: CutoffAgreement): CutoffAgreementView {
    return CutoffAgreementView(
        code = item.code,
        title = truncate(item.title, 120),
        region = item.region,
        version = item.version.toString(),
        minimumText = "%.2f".format(item.minimum),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffAgreementViews(items: MutableList<CutoffAgreement>): MutableList<CutoffAgreementView> {
    val views = mutableListOf<CutoffAgreementView>()
    for (item in items) {
        views.add(toCutoffAgreementView(item))
    }
    return views
}

fun sumCutoffAgreementView(view: CutoffAgreementView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedVersion.toDouble() + parsedMinimum + parsedRatio
}
