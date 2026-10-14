package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal near miss agreement. */
data class SeasonalNearMissAgreementView(
    var codeText: String,
    var label: String,
    var archived: String,
    var checksum: String,
    var source: String,
    var title: String,
    var retries: String,
    var approved: String,
    var statusLabel: String,
)

fun toSeasonalNearMissAgreementView(item: SeasonalNearMissAgreement): SeasonalNearMissAgreementView {
    return SeasonalNearMissAgreementView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 80),
        archived = "${item.archived}",
        checksum = truncate(item.checksum, 80),
        source = item.source,
        title = item.title,
        retries = item.retries.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNearMissAgreementViews(items: MutableList<SeasonalNearMissAgreement>): MutableList<SeasonalNearMissAgreementView> {
    val views = mutableListOf<SeasonalNearMissAgreementView>()
    for (item in items) {
        views.add(toSeasonalNearMissAgreementView(item))
    }
    return views
}

fun sumSeasonalNearMissAgreementView(view: SeasonalNearMissAgreementView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
