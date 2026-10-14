package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional declaration template. */
data class RegionalDeclarationTemplateView(
    var code: String,
    var label: String,
    var capacity: String,
    var revisionText: String,
    var contactEmailText: String,
    var amount: String,
    var quantity: String,
    var stopCountText: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toRegionalDeclarationTemplateView(item: RegionalDeclarationTemplate): RegionalDeclarationTemplateView {
    return RegionalDeclarationTemplateView(
        code = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        revisionText = item.revision.toString(),
        contactEmailText = truncate(item.contactEmail, 64),
        amount = "%.2f".format(item.amount),
        quantity = item.quantity.toString(),
        stopCountText = item.stopCount.toString(),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDeclarationTemplateViews(items: MutableList<RegionalDeclarationTemplate>): MutableList<RegionalDeclarationTemplateView> {
    val views = mutableListOf<RegionalDeclarationTemplateView>()
    for (item in items) {
        views.add(toRegionalDeclarationTemplateView(item))
    }
    return views
}

fun sumRegionalDeclarationTemplateView(view: RegionalDeclarationTemplateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedAmount
}
