package com.acme.transport.customs.declaration.audit

/** How screens and exports show a declaration audit. */
data class DeclarationAuditView(
    var code: String,
    var title: String,
    var createdAt: String,
    var carrier: String,
    var costText: String,
    var approved: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toDeclarationAuditView(item: DeclarationAudit): DeclarationAuditView {
    return DeclarationAuditView(
        code = item.code,
        title = item.title,
        createdAt = item.createdAt.toString(),
        carrier = item.carrier,
        costText = "%.2f".format(item.cost),
        approved = "${item.approved}",
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationAuditViews(items: MutableList<DeclarationAudit>): MutableList<DeclarationAuditView> {
    val views = mutableListOf<DeclarationAuditView>()
    for (item in items) {
        views.add(toDeclarationAuditView(item))
    }
    return views
}

fun sumDeclarationAuditView(view: DeclarationAuditView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCreatedAt.toDouble() + parsedCost + parsedLoadKg
}
