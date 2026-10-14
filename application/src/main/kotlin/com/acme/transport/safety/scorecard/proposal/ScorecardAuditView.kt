package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard audit. */
data class ScorecardAuditView(
    var code: String,
    var titleText: String,
    var contactEmail: String,
    var endsAtText: String,
    var archivedText: String,
    var localeText: String,
    var visible: String,
    var rateText: String,
    var fuelLiters: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toScorecardAuditView(item: ScorecardAudit): ScorecardAuditView {
    return ScorecardAuditView(
        code = item.code,
        titleText = item.title,
        contactEmail = item.contactEmail,
        endsAtText = item.endsAt.toString(),
        archivedText = "${item.archived}",
        localeText = truncate(item.locale, 64),
        visible = "${item.visible}",
        rateText = "%.2f".format(item.rate),
        fuelLiters = "%.2f".format(item.fuelLiters),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardAuditViews(items: MutableList<ScorecardAudit>): MutableList<ScorecardAuditView> {
    val views = mutableListOf<ScorecardAuditView>()
    for (item in items) {
        views.add(toScorecardAuditView(item))
    }
    return views
}

fun sumScorecardAuditView(view: ScorecardAuditView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedEndsAt.toDouble() + parsedRate + parsedFuelLiters
}
