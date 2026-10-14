package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual broker reservation. */
data class ManualBrokerReservationView(
    var code: String,
    var title: String,
    var localeText: String,
    var ownerIdText: String,
    var phone: String,
    var visible: String,
    var version: String,
    var statusLabel: String,
)

fun toManualBrokerReservationView(item: ManualBrokerReservation): ManualBrokerReservationView {
    return ManualBrokerReservationView(
        code = item.code,
        title = truncate(item.title, 120),
        localeText = item.locale,
        ownerIdText = item.ownerId,
        phone = item.phone,
        visible = "${item.visible}",
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualBrokerReservationViews(items: MutableList<ManualBrokerReservation>): MutableList<ManualBrokerReservationView> {
    val views = mutableListOf<ManualBrokerReservationView>()
    for (item in items) {
        views.add(toManualBrokerReservationView(item))
    }
    return views
}

fun sumManualBrokerReservationView(view: ManualBrokerReservationView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
