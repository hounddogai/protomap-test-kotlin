package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default roster agreement. */
data class DefaultRosterAgreementView(
    var code: String,
    var labelText: String,
    var laneText: String,
    var batchSize: String,
    var stopCount: String,
    var minimum: String,
    var contactEmail: String,
    var unit: String,
    var shiftCode: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDefaultRosterAgreementView(item: DefaultRosterAgreement): DefaultRosterAgreementView {
    return DefaultRosterAgreementView(
        code = item.code,
        labelText = truncate(item.label, 80),
        laneText = item.lane,
        batchSize = item.batchSize.toString(),
        stopCount = item.stopCount.toString(),
        minimum = "%.2f".format(item.minimum),
        contactEmail = item.contactEmail,
        unit = truncate(item.unit, 40),
        shiftCode = item.shiftCode,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRosterAgreementViews(items: MutableList<DefaultRosterAgreement>): MutableList<DefaultRosterAgreementView> {
    val views = mutableListOf<DefaultRosterAgreementView>()
    for (item in items) {
        views.add(toDefaultRosterAgreementView(item))
    }
    return views
}

fun sumDefaultRosterAgreementView(view: DefaultRosterAgreementView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBatchSize.toDouble() + parsedStopCount.toDouble() + parsedMinimum
}
