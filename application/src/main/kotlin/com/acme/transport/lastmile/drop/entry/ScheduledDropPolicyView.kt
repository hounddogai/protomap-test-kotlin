package com.acme.transport.lastmile.drop.entry

/** How screens and exports show a scheduled drop policy. */
data class ScheduledDropPolicyView(
    var codeText: String,
    var name: String,
    var createdAtText: String,
    var revision: String,
    var rate: String,
    var dock: String,
    var pallets: String,
    var odometer: String,
    var statusLabel: String,
)

fun toScheduledDropPolicyView(item: ScheduledDropPolicy): ScheduledDropPolicyView {
    return ScheduledDropPolicyView(
        codeText = item.code,
        name = item.name,
        createdAtText = item.createdAt.toString(),
        revision = item.revision.toString(),
        rate = "%.2f".format(item.rate),
        dock = item.dock,
        pallets = item.pallets.toString(),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDropPolicyViews(items: MutableList<ScheduledDropPolicy>): MutableList<ScheduledDropPolicyView> {
    val views = mutableListOf<ScheduledDropPolicyView>()
    for (item in items) {
        views.add(toScheduledDropPolicyView(item))
    }
    return views
}

fun sumScheduledDropPolicyView(view: ScheduledDropPolicyView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCreatedAt.toDouble() + parsedRevision.toDouble() + parsedRate
}
