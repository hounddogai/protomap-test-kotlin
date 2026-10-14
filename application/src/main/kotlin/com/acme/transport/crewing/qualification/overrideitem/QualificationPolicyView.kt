package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification policy. */
data class QualificationPolicyView(
    var codeText: String,
    var label: String,
    var distanceKmText: String,
    var ceiling: String,
    var createdAt: String,
    var driverIdText: String,
    var endsAt: String,
    var loadKg: String,
    var version: String,
    var statusLabel: String,
)

fun toQualificationPolicyView(item: QualificationPolicy): QualificationPolicyView {
    return QualificationPolicyView(
        codeText = item.code,
        label = truncate(item.label, 64),
        distanceKmText = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        driverIdText = truncate(item.driverId, 120),
        endsAt = item.endsAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationPolicyViews(items: MutableList<QualificationPolicy>): MutableList<QualificationPolicyView> {
    val views = mutableListOf<QualificationPolicyView>()
    for (item in items) {
        views.add(toQualificationPolicyView(item))
    }
    return views
}

fun sumQualificationPolicyView(view: QualificationPolicyView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedDistanceKm + parsedCeiling + parsedCreatedAt.toDouble()
}
