package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial agreement. */
data class AccessorialAgreementView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var versionText: String,
    var capacity: String,
    var fuelLiters: String,
    var dockText: String,
    var lane: String,
    var minimum: String,
    var description: String,
    var statusLabel: String,
)

fun toAccessorialAgreementView(item: AccessorialAgreement): AccessorialAgreementView {
    return AccessorialAgreementView(
        codeText = truncate(item.code, 64),
        name = item.name,
        batchSize = item.batchSize.toString(),
        versionText = item.version.toString(),
        capacity = item.capacity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        dockText = item.dock,
        lane = item.lane,
        minimum = "%.2f".format(item.minimum),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialAgreementViews(items: MutableList<AccessorialAgreement>): MutableList<AccessorialAgreementView> {
    val views = mutableListOf<AccessorialAgreementView>()
    for (item in items) {
        views.add(toAccessorialAgreementView(item))
    }
    return views
}

fun sumAccessorialAgreementView(view: AccessorialAgreementView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedBatchSize.toDouble() + parsedVersion.toDouble() + parsedCapacity.toDouble()
}
