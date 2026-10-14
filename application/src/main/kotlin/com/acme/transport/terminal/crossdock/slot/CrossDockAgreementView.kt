package com.acme.transport.terminal.crossdock.slot

/** How screens and exports show a cross dock agreement. */
data class CrossDockAgreementView(
    var codeText: String,
    var name: String,
    var budget: String,
    var capacity: String,
    var active: String,
    var source: String,
    var summary: String,
    var graceDaysText: String,
    var pinned: String,
    var unit: String,
    var statusLabel: String,
)

fun toCrossDockAgreementView(item: CrossDockAgreement): CrossDockAgreementView {
    return CrossDockAgreementView(
        codeText = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        capacity = item.capacity.toString(),
        active = "${item.active}",
        source = item.source,
        summary = item.summary,
        graceDaysText = item.graceDays.toString(),
        pinned = "${item.pinned}",
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockAgreementViews(items: MutableList<CrossDockAgreement>): MutableList<CrossDockAgreementView> {
    val views = mutableListOf<CrossDockAgreementView>()
    for (item in items) {
        views.add(toCrossDockAgreementView(item))
    }
    return views
}

fun sumCrossDockAgreementView(view: CrossDockAgreementView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedBudget + parsedCapacity.toDouble() + parsedGraceDays.toDouble()
}
