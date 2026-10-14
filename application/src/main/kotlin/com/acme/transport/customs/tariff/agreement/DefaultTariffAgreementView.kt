package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tariff agreement. */
data class DefaultTariffAgreementView(
    var code: String,
    var name: String,
    var limit: String,
    var visible: String,
    var ownerIdText: String,
    var countryText: String,
    var vehicleId: String,
    var summary: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toDefaultTariffAgreementView(item: DefaultTariffAgreement): DefaultTariffAgreementView {
    return DefaultTariffAgreementView(
        code = truncate(item.code, 80),
        name = item.name,
        limit = item.limit.toString(),
        visible = "${item.visible}",
        ownerIdText = item.ownerId,
        countryText = truncate(item.country, 64),
        vehicleId = item.vehicleId,
        summary = item.summary,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTariffAgreementViews(items: MutableList<DefaultTariffAgreement>): MutableList<DefaultTariffAgreementView> {
    val views = mutableListOf<DefaultTariffAgreementView>()
    for (item in items) {
        views.add(toDefaultTariffAgreementView(item))
    }
    return views
}

fun sumDefaultTariffAgreementView(view: DefaultTariffAgreementView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedLimit.toDouble() + parsedBatchSize.toDouble()
}
