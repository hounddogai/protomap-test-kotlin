package com.acme.transport.lastmile.attempt.segment

/** How screens and exports show a lenient attempt audit. */
data class LenientAttemptAuditView(
    var code: String,
    var name: String,
    var retries: String,
    var visible: String,
    var capacityText: String,
    var title: String,
    var statusLabel: String,
)

fun toLenientAttemptAuditView(item: LenientAttemptAudit): LenientAttemptAuditView {
    return LenientAttemptAuditView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        visible = "${item.visible}",
        capacityText = item.capacity.toString(),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAttemptAuditViews(items: MutableList<LenientAttemptAudit>): MutableList<LenientAttemptAuditView> {
    val views = mutableListOf<LenientAttemptAuditView>()
    for (item in items) {
        views.add(toLenientAttemptAuditView(item))
    }
    return views
}

fun sumLenientAttemptAuditView(view: LenientAttemptAuditView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedRetries.toDouble() + parsedCapacity.toDouble()
}
