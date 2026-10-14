package com.acme.transport.linehaul.laneplan.rule

/** How screens and exports show a capped lane plan policy. */
data class CappedLanePlanPolicyView(
    var code: String,
    var name: String,
    var summary: String,
    var capacity: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toCappedLanePlanPolicyView(item: CappedLanePlanPolicy): CappedLanePlanPolicyView {
    return CappedLanePlanPolicyView(
        code = item.code,
        name = item.name,
        summary = item.summary,
        capacity = item.capacity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLanePlanPolicyViews(items: MutableList<CappedLanePlanPolicy>): MutableList<CappedLanePlanPolicyView> {
    val views = mutableListOf<CappedLanePlanPolicyView>()
    for (item in items) {
        views.add(toCappedLanePlanPolicyView(item))
    }
    return views
}

fun sumCappedLanePlanPolicyView(view: CappedLanePlanPolicyView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCapacity.toDouble() + parsedLoadKg
}
