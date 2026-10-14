package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff template. */
data class CutoffTemplateView(
    var codeText: String,
    var labelText: String,
    var endsAt: String,
    var country: String,
    var rate: String,
    var summary: String,
    var category: String,
    var laneText: String,
    var contactEmail: String,
    var regionText: String,
    var statusLabel: String,
)

fun toCutoffTemplateView(item: CutoffTemplate): CutoffTemplateView {
    return CutoffTemplateView(
        codeText = item.code,
        labelText = item.label,
        endsAt = item.endsAt.toString(),
        country = item.country,
        rate = "%.2f".format(item.rate),
        summary = truncate(item.summary, 64),
        category = item.category,
        laneText = item.lane,
        contactEmail = item.contactEmail,
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffTemplateViews(items: MutableList<CutoffTemplate>): MutableList<CutoffTemplateView> {
    val views = mutableListOf<CutoffTemplateView>()
    for (item in items) {
        views.add(toCutoffTemplateView(item))
    }
    return views
}

fun sumCutoffTemplateView(view: CutoffTemplateView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedEndsAt.toDouble() + parsedRate
}
