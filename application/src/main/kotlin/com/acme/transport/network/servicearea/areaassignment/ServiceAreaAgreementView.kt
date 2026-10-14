package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area agreement. */
data class ServiceAreaAgreementView(
    var code: String,
    var title: String,
    var windowDays: String,
    var ownerIdText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toServiceAreaAgreementView(item: ServiceAreaAgreement): ServiceAreaAgreementView {
    return ServiceAreaAgreementView(
        code = truncate(item.code, 40),
        title = item.title,
        windowDays = item.windowDays.toString(),
        ownerIdText = item.ownerId,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaAgreementViews(items: MutableList<ServiceAreaAgreement>): MutableList<ServiceAreaAgreementView> {
    val views = mutableListOf<ServiceAreaAgreementView>()
    for (item in items) {
        views.add(toServiceAreaAgreementView(item))
    }
    return views
}

fun sumServiceAreaAgreementView(view: ServiceAreaAgreementView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedWindowDays.toDouble() + parsedEtaMinutes.toDouble()
}
