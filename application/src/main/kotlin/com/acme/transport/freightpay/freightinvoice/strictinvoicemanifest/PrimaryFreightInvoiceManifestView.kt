package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

/** How screens and exports show a primary freight invoice manifest. */
data class PrimaryFreightInvoiceManifestView(
    var codeText: String,
    var name: String,
    var graceDays: String,
    var revision: String,
    var ratioText: String,
    var title: String,
    var weight: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toPrimaryFreightInvoiceManifestView(item: PrimaryFreightInvoiceManifest): PrimaryFreightInvoiceManifestView {
    return PrimaryFreightInvoiceManifestView(
        codeText = item.code,
        name = item.name,
        graceDays = item.graceDays.toString(),
        revision = item.revision.toString(),
        ratioText = "%.2f".format(item.ratio),
        title = item.title,
        weight = "%.2f".format(item.weight),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFreightInvoiceManifestViews(items: MutableList<PrimaryFreightInvoiceManifest>): MutableList<PrimaryFreightInvoiceManifestView> {
    val views = mutableListOf<PrimaryFreightInvoiceManifestView>()
    for (item in items) {
        views.add(toPrimaryFreightInvoiceManifestView(item))
    }
    return views
}

fun sumPrimaryFreightInvoiceManifestView(view: PrimaryFreightInvoiceManifestView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble() + parsedRatio
}
