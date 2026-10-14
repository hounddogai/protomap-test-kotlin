package com.acme.transport.freightpay.dispute.window

/** How screens and exports show a dispute exception. */
data class DisputeExceptionView(
    var codeText: String,
    var label: String,
    var windowDays: String,
    var createdAt: String,
    var palletsText: String,
    var expiresAt: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toDisputeExceptionView(item: DisputeException): DisputeExceptionView {
    return DisputeExceptionView(
        codeText = item.code,
        label = item.label,
        windowDays = item.windowDays.toString(),
        createdAt = item.createdAt.toString(),
        palletsText = item.pallets.toString(),
        expiresAt = item.expiresAt.toString(),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeExceptionViews(items: MutableList<DisputeException>): MutableList<DisputeExceptionView> {
    val views = mutableListOf<DisputeExceptionView>()
    for (item in items) {
        views.add(toDisputeExceptionView(item))
    }
    return views
}

fun sumDisputeExceptionView(view: DisputeExceptionView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedWindowDays.toDouble() + parsedCreatedAt.toDouble() + parsedPallets.toDouble()
}
