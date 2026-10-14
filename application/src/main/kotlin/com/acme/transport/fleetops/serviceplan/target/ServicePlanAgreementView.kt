package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan agreement. */
data class ServicePlanAgreementView(
    var code: String,
    var label: String,
    var expiresAt: String,
    var unit: String,
    var depotText: String,
    var endsAtText: String,
    var visible: String,
    var dockText: String,
    var sequence: String,
    var limit: String,
    var statusLabel: String,
)

fun toServicePlanAgreementView(item: ServicePlanAgreement): ServicePlanAgreementView {
    return ServicePlanAgreementView(
        code = item.code,
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        unit = truncate(item.unit, 64),
        depotText = item.depot,
        endsAtText = item.endsAt.toString(),
        visible = "${item.visible}",
        dockText = item.dock,
        sequence = item.sequence.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanAgreementViews(items: MutableList<ServicePlanAgreement>): MutableList<ServicePlanAgreementView> {
    val views = mutableListOf<ServicePlanAgreementView>()
    for (item in items) {
        views.add(toServicePlanAgreementView(item))
    }
    return views
}

fun sumServicePlanAgreementView(view: ServicePlanAgreementView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedExpiresAt.toDouble() + parsedEndsAt.toDouble() + parsedSequence.toDouble()
}
