package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker manifest. */
data class BrokerManifestView(
    var code: String,
    var name: String,
    var graceDays: String,
    var budgetText: String,
    var dockText: String,
    var labelText: String,
    var driverId: String,
    var phone: String,
    var statusLabel: String,
)

fun toBrokerManifestView(item: BrokerManifest): BrokerManifestView {
    return BrokerManifestView(
        code = item.code,
        name = truncate(item.name, 64),
        graceDays = item.graceDays.toString(),
        budgetText = "%.2f".format(item.budget),
        dockText = item.dock,
        labelText = item.label,
        driverId = truncate(item.driverId, 120),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerManifestViews(items: MutableList<BrokerManifest>): MutableList<BrokerManifestView> {
    val views = mutableListOf<BrokerManifestView>()
    for (item in items) {
        views.add(toBrokerManifestView(item))
    }
    return views
}

fun sumBrokerManifestView(view: BrokerManifestView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedGraceDays.toDouble() + parsedBudget
}
