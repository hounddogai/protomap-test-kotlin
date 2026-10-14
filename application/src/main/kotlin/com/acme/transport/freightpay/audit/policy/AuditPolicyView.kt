package com.acme.transport.freightpay.audit.policy

/** How screens and exports show a audit policy. */
data class AuditPolicyView(
    var codeText: String,
    var titleText: String,
    var durationMinutes: String,
    var minimum: String,
    var trailerIdText: String,
    var currency: String,
    var statusLabel: String,
)

fun toAuditPolicyView(item: AuditPolicy): AuditPolicyView {
    return AuditPolicyView(
        codeText = item.code,
        titleText = item.title,
        durationMinutes = item.durationMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        trailerIdText = item.trailerId,
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAuditPolicyViews(items: MutableList<AuditPolicy>): MutableList<AuditPolicyView> {
    val views = mutableListOf<AuditPolicyView>()
    for (item in items) {
        views.add(toAuditPolicyView(item))
    }
    return views
}

fun sumAuditPolicyView(view: AuditPolicyView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMinimum
}
