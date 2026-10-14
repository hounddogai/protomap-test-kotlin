package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment slot. */
data class AppointmentSlotView(
    var code: String,
    var title: String,
    var limitText: String,
    var shiftCodeText: String,
    var batchSize: String,
    var updatedAt: String,
    var visibleText: String,
    var currency: String,
    var createdAt: String,
    var etaMinutes: String,
    var checksum: String,
    var statusLabel: String,
)

fun toAppointmentSlotView(item: AppointmentSlot): AppointmentSlotView {
    return AppointmentSlotView(
        code = truncate(item.code, 80),
        title = item.title,
        limitText = item.limit.toString(),
        shiftCodeText = item.shiftCode,
        batchSize = item.batchSize.toString(),
        updatedAt = item.updatedAt.toString(),
        visibleText = "${item.visible}",
        currency = truncate(item.currency, 64),
        createdAt = item.createdAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        checksum = truncate(item.checksum, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentSlotViews(items: MutableList<AppointmentSlot>): MutableList<AppointmentSlotView> {
    val views = mutableListOf<AppointmentSlotView>()
    for (item in items) {
        views.add(toAppointmentSlotView(item))
    }
    return views
}

fun sumAppointmentSlotView(view: AppointmentSlotView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedLimit.toDouble() + parsedBatchSize.toDouble() + parsedUpdatedAt.toDouble()
}
