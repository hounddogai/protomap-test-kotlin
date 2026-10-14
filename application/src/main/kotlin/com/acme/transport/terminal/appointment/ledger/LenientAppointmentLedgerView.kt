package com.acme.transport.terminal.appointment.ledger

/** How screens and exports show a lenient appointment ledger. */
data class LenientAppointmentLedgerView(
    var codeText: String,
    var name: String,
    var shiftCode: String,
    var floorValue: String,
    var startsAt: String,
    var budget: String,
    var statusLabel: String,
)

fun toLenientAppointmentLedgerView(item: LenientAppointmentLedger): LenientAppointmentLedgerView {
    return LenientAppointmentLedgerView(
        codeText = item.code,
        name = item.name,
        shiftCode = item.shiftCode,
        floorValue = "%.2f".format(item.floorValue),
        startsAt = item.startsAt.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAppointmentLedgerViews(items: MutableList<LenientAppointmentLedger>): MutableList<LenientAppointmentLedgerView> {
    val views = mutableListOf<LenientAppointmentLedgerView>()
    for (item in items) {
        views.add(toLenientAppointmentLedgerView(item))
    }
    return views
}

fun sumLenientAppointmentLedgerView(view: LenientAppointmentLedgerView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedBudget
}
