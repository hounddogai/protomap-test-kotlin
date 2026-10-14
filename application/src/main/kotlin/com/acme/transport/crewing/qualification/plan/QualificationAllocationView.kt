package com.acme.transport.crewing.qualification.plan

/** How screens and exports show a qualification allocation. */
data class QualificationAllocationView(
    var codeText: String,
    var nameText: String,
    var label: String,
    var approved: String,
    var checksum: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toQualificationAllocationView(item: QualificationAllocation): QualificationAllocationView {
    return QualificationAllocationView(
        codeText = item.code,
        nameText = item.name,
        label = item.label,
        approved = "${item.approved}",
        checksum = item.checksum,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationAllocationViews(items: MutableList<QualificationAllocation>): MutableList<QualificationAllocationView> {
    val views = mutableListOf<QualificationAllocationView>()
    for (item in items) {
        views.add(toQualificationAllocationView(item))
    }
    return views
}

fun sumQualificationAllocationView(view: QualificationAllocationView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
