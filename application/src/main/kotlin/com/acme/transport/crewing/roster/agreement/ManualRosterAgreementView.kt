package com.acme.transport.crewing.roster.agreement

/** How screens and exports show a manual roster agreement. */
data class ManualRosterAgreementView(
    var code: String,
    var labelText: String,
    var expiresAt: String,
    var phoneText: String,
    var startsAt: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toManualRosterAgreementView(item: ManualRosterAgreement): ManualRosterAgreementView {
    return ManualRosterAgreementView(
        code = item.code,
        labelText = item.label,
        expiresAt = item.expiresAt.toString(),
        phoneText = item.phone,
        startsAt = item.startsAt.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRosterAgreementViews(items: MutableList<ManualRosterAgreement>): MutableList<ManualRosterAgreementView> {
    val views = mutableListOf<ManualRosterAgreementView>()
    for (item in items) {
        views.add(toManualRosterAgreementView(item))
    }
    return views
}

fun sumManualRosterAgreementView(view: ManualRosterAgreementView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedExpiresAt.toDouble() + parsedStartsAt.toDouble() + parsedCapacity.toDouble()
}
