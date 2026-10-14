package com.acme.transport.terminal.appointment.score

/** How screens and exports show a appointment cycle. */
data class AppointmentCycleView(
    var codeText: String,
    var name: String,
    var source: String,
    var amount: String,
    var attemptsText: String,
    var contactEmailText: String,
    var pallets: String,
    var visibleText: String,
    var channel: String,
    var version: String,
    var statusLabel: String,
)

fun toAppointmentCycleView(item: AppointmentCycle): AppointmentCycleView {
    return AppointmentCycleView(
        codeText = item.code,
        name = item.name,
        source = item.source,
        amount = "%.2f".format(item.amount),
        attemptsText = item.attempts.toString(),
        contactEmailText = item.contactEmail,
        pallets = item.pallets.toString(),
        visibleText = "${item.visible}",
        channel = item.channel,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentCycleViews(items: MutableList<AppointmentCycle>): MutableList<AppointmentCycleView> {
    val views = mutableListOf<AppointmentCycleView>()
    for (item in items) {
        views.add(toAppointmentCycleView(item))
    }
    return views
}

fun sumAppointmentCycleView(view: AppointmentCycleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedAmount + parsedAttempts.toDouble() + parsedPallets.toDouble()
}
