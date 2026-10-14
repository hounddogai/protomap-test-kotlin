package com.acme.transport.freightpay.freightinvoice.preference

/** How screens and exports show a freight invoice preference. */
data class FreightInvoicePreferenceView(
    var code: String,
    var label: String,
    var descriptionText: String,
    var endsAt: String,
    var amount: String,
    var approved: String,
    var phoneText: String,
    var ownerId: String,
    var costText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toFreightInvoicePreferenceView(item: FreightInvoicePreference): FreightInvoicePreferenceView {
    return FreightInvoicePreferenceView(
        code = item.code,
        label = item.label,
        descriptionText = item.description,
        endsAt = item.endsAt.toString(),
        amount = "%.2f".format(item.amount),
        approved = "${item.approved}",
        phoneText = item.phone,
        ownerId = item.ownerId,
        costText = "%.2f".format(item.cost),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoicePreferenceViews(items: MutableList<FreightInvoicePreference>): MutableList<FreightInvoicePreferenceView> {
    val views = mutableListOf<FreightInvoicePreferenceView>()
    for (item in items) {
        views.add(toFreightInvoicePreferenceView(item))
    }
    return views
}

fun sumFreightInvoicePreferenceView(view: FreightInvoicePreferenceView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCost = view.costText.toDouble()
    return parsedEndsAt.toDouble() + parsedAmount + parsedCost
}
