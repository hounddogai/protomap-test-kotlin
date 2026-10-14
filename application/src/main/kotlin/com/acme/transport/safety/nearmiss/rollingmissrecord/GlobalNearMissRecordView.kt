package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global near miss record. */
data class GlobalNearMissRecordView(
    var codeText: String,
    var name: String,
    var loadKg: String,
    var vehicleId: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalNearMissRecordView(item: GlobalNearMissRecord): GlobalNearMissRecordView {
    return GlobalNearMissRecordView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 120),
        loadKg = "%.2f".format(item.loadKg),
        vehicleId = item.vehicleId,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalNearMissRecordViews(items: MutableList<GlobalNearMissRecord>): MutableList<GlobalNearMissRecordView> {
    val views = mutableListOf<GlobalNearMissRecordView>()
    for (item in items) {
        views.add(toGlobalNearMissRecordView(item))
    }
    return views
}

fun sumGlobalNearMissRecordView(view: GlobalNearMissRecordView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
