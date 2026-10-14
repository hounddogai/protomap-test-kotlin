package com.acme.transport.customs.declaration.milestone

/** How screens and exports show a declaration agreement. */
data class DeclarationAgreementView(
    var code: String,
    var title: String,
    var channelText: String,
    var vehicleIdText: String,
    var reviewer: String,
    var externalRef: String,
    var active: String,
    var ownerId: String,
    var summary: String,
    var amount: String,
    var statusLabel: String,
)

fun toDeclarationAgreementView(item: DeclarationAgreement): DeclarationAgreementView {
    return DeclarationAgreementView(
        code = item.code,
        title = item.title,
        channelText = item.channel,
        vehicleIdText = item.vehicleId,
        reviewer = item.reviewer,
        externalRef = item.externalRef,
        active = "${item.active}",
        ownerId = item.ownerId,
        summary = item.summary,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationAgreementViews(items: MutableList<DeclarationAgreement>): MutableList<DeclarationAgreementView> {
    val views = mutableListOf<DeclarationAgreementView>()
    for (item in items) {
        views.add(toDeclarationAgreementView(item))
    }
    return views
}

fun sumDeclarationAgreementView(view: DeclarationAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
