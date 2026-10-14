package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default accessorial cycle. */
data class DefaultAccessorialCycleView(
    var code: String,
    var name: String,
    var versionText: String,
    var routeCode: String,
    var etaMinutes: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toDefaultAccessorialCycleView(item: DefaultAccessorialCycle): DefaultAccessorialCycleView {
    return DefaultAccessorialCycleView(
        code = truncate(item.code, 40),
        name = item.name,
        versionText = item.version.toString(),
        routeCode = item.routeCode,
        etaMinutes = item.etaMinutes.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAccessorialCycleViews(items: MutableList<DefaultAccessorialCycle>): MutableList<DefaultAccessorialCycleView> {
    val views = mutableListOf<DefaultAccessorialCycleView>()
    for (item in items) {
        views.add(toDefaultAccessorialCycleView(item))
    }
    return views
}

fun sumDefaultAccessorialCycleView(view: DefaultAccessorialCycleView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedVersion.toDouble() + parsedEtaMinutes.toDouble() + parsedFuelLiters
}
