package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered qualification profile. */
data class TieredQualificationProfileView(
    var codeText: String,
    var nameText: String,
    var score: String,
    var etaMinutes: String,
    var sourceText: String,
    var approvedText: String,
    var phone: String,
    var statusLabel: String,
)

fun toTieredQualificationProfileView(item: TieredQualificationProfile): TieredQualificationProfileView {
    return TieredQualificationProfileView(
        codeText = item.code,
        nameText = truncate(item.name, 64),
        score = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        sourceText = item.source,
        approvedText = "${item.approved}",
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredQualificationProfileViews(items: MutableList<TieredQualificationProfile>): MutableList<TieredQualificationProfileView> {
    val views = mutableListOf<TieredQualificationProfileView>()
    for (item in items) {
        views.add(toTieredQualificationProfileView(item))
    }
    return views
}

fun sumTieredQualificationProfileView(view: TieredQualificationProfileView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedScore + parsedEtaMinutes.toDouble()
}
