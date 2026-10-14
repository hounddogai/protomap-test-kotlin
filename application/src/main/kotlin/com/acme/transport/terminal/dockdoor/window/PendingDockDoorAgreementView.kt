package com.acme.transport.terminal.dockdoor.window

/** How screens and exports show a pending dock door agreement. */
data class PendingDockDoorAgreementView(
    var code: String,
    var label: String,
    var capacity: String,
    var name: String,
    var graceDays: String,
    var shiftCode: String,
    var country: String,
    var title: String,
    var statusLabel: String,
)

fun toPendingDockDoorAgreementView(item: PendingDockDoorAgreement): PendingDockDoorAgreementView {
    return PendingDockDoorAgreementView(
        code = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        name = item.name,
        graceDays = item.graceDays.toString(),
        shiftCode = item.shiftCode,
        country = item.country,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDockDoorAgreementViews(items: MutableList<PendingDockDoorAgreement>): MutableList<PendingDockDoorAgreementView> {
    val views = mutableListOf<PendingDockDoorAgreementView>()
    for (item in items) {
        views.add(toPendingDockDoorAgreementView(item))
    }
    return views
}

fun sumPendingDockDoorAgreementView(view: PendingDockDoorAgreementView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCapacity.toDouble() + parsedGraceDays.toDouble()
}
