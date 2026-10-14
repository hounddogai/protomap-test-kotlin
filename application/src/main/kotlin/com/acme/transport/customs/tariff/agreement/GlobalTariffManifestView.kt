package com.acme.transport.customs.tariff.agreement

/** How screens and exports show a global tariff manifest. */
data class GlobalTariffManifestView(
    var code: String,
    var name: String,
    var maximum: String,
    var priorityText: String,
    var thresholdText: String,
    var dockText: String,
    var limit: String,
    var graceDays: String,
    var driverId: String,
    var weight: String,
    var statusLabel: String,
)

fun toGlobalTariffManifestView(item: GlobalTariffManifest): GlobalTariffManifestView {
    return GlobalTariffManifestView(
        code = item.code,
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        priorityText = item.priority.toString(),
        thresholdText = "%.2f".format(item.threshold),
        dockText = item.dock,
        limit = item.limit.toString(),
        graceDays = item.graceDays.toString(),
        driverId = item.driverId,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTariffManifestViews(items: MutableList<GlobalTariffManifest>): MutableList<GlobalTariffManifestView> {
    val views = mutableListOf<GlobalTariffManifestView>()
    for (item in items) {
        views.add(toGlobalTariffManifestView(item))
    }
    return views
}

fun sumGlobalTariffManifestView(view: GlobalTariffManifestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedMaximum + parsedPriority.toDouble() + parsedThreshold
}
