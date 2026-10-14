package com.acme.transport.network.cutoff.regionalcutoffsegment

/** How screens and exports show a rolling cutoff audit. */
data class RollingCutoffAuditView(
    var code: String,
    var titleText: String,
    var version: String,
    var amount: String,
    var revision: String,
    var visible: String,
    var source: String,
    var locale: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toRollingCutoffAuditView(item: RollingCutoffAudit): RollingCutoffAuditView {
    return RollingCutoffAuditView(
        code = item.code,
        titleText = item.title,
        version = item.version.toString(),
        amount = "%.2f".format(item.amount),
        revision = item.revision.toString(),
        visible = "${item.visible}",
        source = item.source,
        locale = item.locale,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCutoffAuditViews(items: MutableList<RollingCutoffAudit>): MutableList<RollingCutoffAuditView> {
    val views = mutableListOf<RollingCutoffAuditView>()
    for (item in items) {
        views.add(toRollingCutoffAuditView(item))
    }
    return views
}

fun sumRollingCutoffAuditView(view: RollingCutoffAuditView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedVersion.toDouble() + parsedAmount + parsedRevision.toDouble()
}
