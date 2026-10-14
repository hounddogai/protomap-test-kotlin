package com.acme.transport.customs.broker.brokerslot

/** How screens and exports show a global broker agreement. */
data class GlobalBrokerAgreementView(
    var codeText: String,
    var label: String,
    var quantity: String,
    var source: String,
    var locale: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toGlobalBrokerAgreementView(item: GlobalBrokerAgreement): GlobalBrokerAgreementView {
    return GlobalBrokerAgreementView(
        codeText = item.code,
        label = item.label,
        quantity = item.quantity.toString(),
        source = item.source,
        locale = item.locale,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBrokerAgreementViews(items: MutableList<GlobalBrokerAgreement>): MutableList<GlobalBrokerAgreementView> {
    val views = mutableListOf<GlobalBrokerAgreementView>()
    for (item in items) {
        views.add(toGlobalBrokerAgreementView(item))
    }
    return views
}

fun sumGlobalBrokerAgreementView(view: GlobalBrokerAgreementView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}
