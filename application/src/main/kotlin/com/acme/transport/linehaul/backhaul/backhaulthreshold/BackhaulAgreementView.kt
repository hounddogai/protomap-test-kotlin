package com.acme.transport.linehaul.backhaul.backhaulthreshold

/** How screens and exports show a backhaul agreement. */
data class BackhaulAgreementView(
    var codeText: String,
    var label: String,
    var externalRefText: String,
    var updatedAt: String,
    var ceiling: String,
    var ratioText: String,
    var reviewer: String,
    var durationMinutes: String,
    var endsAtText: String,
    var odometer: String,
    var description: String,
    var statusLabel: String,
)

fun toBackhaulAgreementView(item: BackhaulAgreement): BackhaulAgreementView {
    return BackhaulAgreementView(
        codeText = item.code,
        label = item.label,
        externalRefText = item.externalRef,
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        ratioText = "%.2f".format(item.ratio),
        reviewer = item.reviewer,
        durationMinutes = item.durationMinutes.toString(),
        endsAtText = item.endsAt.toString(),
        odometer = item.odometer.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulAgreementViews(items: MutableList<BackhaulAgreement>): MutableList<BackhaulAgreementView> {
    val views = mutableListOf<BackhaulAgreementView>()
    for (item in items) {
        views.add(toBackhaulAgreementView(item))
    }
    return views
}

fun sumBackhaulAgreementView(view: BackhaulAgreementView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedCeiling + parsedRatio
}
