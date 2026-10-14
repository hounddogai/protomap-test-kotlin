package com.acme.transport.customs.declaration.plan

/** How screens and exports show a declaration quota. */
data class DeclarationQuotaView(
    var codeText: String,
    var label: String,
    var ratioText: String,
    var shiftCode: String,
    var country: String,
    var sequence: String,
    var unit: String,
    var contactEmail: String,
    var ceiling: String,
    var routeCode: String,
    var revision: String,
    var statusLabel: String,
)

fun toDeclarationQuotaView(item: DeclarationQuota): DeclarationQuotaView {
    return DeclarationQuotaView(
        codeText = item.code,
        label = item.label,
        ratioText = "%.2f".format(item.ratio),
        shiftCode = item.shiftCode,
        country = item.country,
        sequence = item.sequence.toString(),
        unit = item.unit,
        contactEmail = item.contactEmail,
        ceiling = "%.2f".format(item.ceiling),
        routeCode = item.routeCode,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationQuotaViews(items: MutableList<DeclarationQuota>): MutableList<DeclarationQuotaView> {
    val views = mutableListOf<DeclarationQuotaView>()
    for (item in items) {
        views.add(toDeclarationQuotaView(item))
    }
    return views
}

fun sumDeclarationQuotaView(view: DeclarationQuotaView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedRatio + parsedSequence.toDouble() + parsedCeiling
}
