package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration claim. */
data class DeclarationClaimView(
    var code: String,
    var name: String,
    var endsAt: String,
    var version: String,
    var locale: String,
    var lane: String,
    var sequence: String,
    var dock: String,
    var currency: String,
    var capacityText: String,
    var amount: String,
    var statusLabel: String,
)

fun toDeclarationClaimView(item: DeclarationClaim): DeclarationClaimView {
    return DeclarationClaimView(
        code = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        version = item.version.toString(),
        locale = item.locale,
        lane = truncate(item.lane, 120),
        sequence = item.sequence.toString(),
        dock = item.dock,
        currency = item.currency,
        capacityText = item.capacity.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationClaimViews(items: MutableList<DeclarationClaim>): MutableList<DeclarationClaimView> {
    val views = mutableListOf<DeclarationClaimView>()
    for (item in items) {
        views.add(toDeclarationClaimView(item))
    }
    return views
}

fun sumDeclarationClaimView(view: DeclarationClaimView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedVersion = view.version.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEndsAt.toDouble() + parsedVersion.toDouble() + parsedSequence.toDouble()
}
