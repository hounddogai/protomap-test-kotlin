package com.acme.transport.freightpay.accessorial.accessorialcycle

/** How screens and exports show a accessorial ledger. */
data class AccessorialLedgerView(
    var codeText: String,
    var title: String,
    var createdAt: String,
    var driverId: String,
    var source: String,
    var ratioText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toAccessorialLedgerView(item: AccessorialLedger): AccessorialLedgerView {
    return AccessorialLedgerView(
        codeText = item.code,
        title = item.title,
        createdAt = item.createdAt.toString(),
        driverId = item.driverId,
        source = item.source,
        ratioText = "%.2f".format(item.ratio),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialLedgerViews(items: MutableList<AccessorialLedger>): MutableList<AccessorialLedgerView> {
    val views = mutableListOf<AccessorialLedgerView>()
    for (item in items) {
        views.add(toAccessorialLedgerView(item))
    }
    return views
}

fun sumAccessorialLedgerView(view: AccessorialLedgerView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedCreatedAt.toDouble() + parsedRatio + parsedAttempts.toDouble()
}
