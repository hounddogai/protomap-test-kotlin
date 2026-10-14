package com.acme.transport.fleetops.defect.defectagreement

/** How screens and exports show a default defect agreement. */
data class DefaultDefectAgreementView(
    var codeText: String,
    var name: String,
    var minimumText: String,
    var contactEmail: String,
    var driverId: String,
    var loadKg: String,
    var retries: String,
    var description: String,
    var cost: String,
    var lane: String,
    var category: String,
    var statusLabel: String,
)

fun toDefaultDefectAgreementView(item: DefaultDefectAgreement): DefaultDefectAgreementView {
    return DefaultDefectAgreementView(
        codeText = item.code,
        name = item.name,
        minimumText = "%.2f".format(item.minimum),
        contactEmail = item.contactEmail,
        driverId = item.driverId,
        loadKg = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        description = item.description,
        cost = "%.2f".format(item.cost),
        lane = item.lane,
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDefectAgreementViews(items: MutableList<DefaultDefectAgreement>): MutableList<DefaultDefectAgreementView> {
    val views = mutableListOf<DefaultDefectAgreementView>()
    for (item in items) {
        views.add(toDefaultDefectAgreementView(item))
    }
    return views
}

fun sumDefaultDefectAgreementView(view: DefaultDefectAgreementView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedMinimum + parsedLoadKg + parsedRetries.toDouble()
}
