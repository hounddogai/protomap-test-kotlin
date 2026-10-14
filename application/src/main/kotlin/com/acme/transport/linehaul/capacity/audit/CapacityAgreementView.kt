package com.acme.transport.linehaul.capacity.audit

/** How screens and exports show a capacity agreement. */
data class CapacityAgreementView(
    var codeText: String,
    var titleText: String,
    var graceDaysText: String,
    var startsAt: String,
    var maximum: String,
    var phone: String,
    var statusLabel: String,
)

fun toCapacityAgreementView(item: CapacityAgreement): CapacityAgreementView {
    return CapacityAgreementView(
        codeText = item.code,
        titleText = item.title,
        graceDaysText = item.graceDays.toString(),
        startsAt = item.startsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityAgreementViews(items: MutableList<CapacityAgreement>): MutableList<CapacityAgreementView> {
    val views = mutableListOf<CapacityAgreementView>()
    for (item in items) {
        views.add(toCapacityAgreementView(item))
    }
    return views
}

fun sumCapacityAgreementView(view: CapacityAgreementView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedGraceDays.toDouble() + parsedStartsAt.toDouble() + parsedMaximum
}
