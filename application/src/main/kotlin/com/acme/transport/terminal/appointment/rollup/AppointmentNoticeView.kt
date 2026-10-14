package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment notice. */
data class AppointmentNoticeView(
    var codeText: String,
    var name: String,
    var locale: String,
    var depot: String,
    var phone: String,
    var quantity: String,
    var statusLabel: String,
)

fun toAppointmentNoticeView(item: AppointmentNotice): AppointmentNoticeView {
    return AppointmentNoticeView(
        codeText = item.code,
        name = item.name,
        locale = item.locale,
        depot = truncate(item.depot, 64),
        phone = truncate(item.phone, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentNoticeViews(items: MutableList<AppointmentNotice>): MutableList<AppointmentNoticeView> {
    val views = mutableListOf<AppointmentNoticeView>()
    for (item in items) {
        views.add(toAppointmentNoticeView(item))
    }
    return views
}

fun sumAppointmentNoticeView(view: AppointmentNoticeView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
