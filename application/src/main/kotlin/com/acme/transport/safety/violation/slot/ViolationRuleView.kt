package com.acme.transport.safety.violation.slot

/** How screens and exports show a violation rule. */
data class ViolationRuleView(
    var code: String,
    var label: String,
    var ownerIdText: String,
    var threshold: String,
    var limit: String,
    var statusLabel: String,
)

fun toViolationRuleView(item: ViolationRule): ViolationRuleView {
    return ViolationRuleView(
        code = item.code,
        label = item.label,
        ownerIdText = item.ownerId,
        threshold = "%.2f".format(item.threshold),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationRuleViews(items: MutableList<ViolationRule>): MutableList<ViolationRuleView> {
    val views = mutableListOf<ViolationRuleView>()
    for (item in items) {
        views.add(toViolationRuleView(item))
    }
    return views
}

fun sumViolationRuleView(view: ViolationRuleView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedThreshold + parsedLimit.toDouble()
}
