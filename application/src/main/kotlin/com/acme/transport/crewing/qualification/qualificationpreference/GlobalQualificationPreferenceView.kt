package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global qualification preference. */
data class GlobalQualificationPreferenceView(
    var code: String,
    var name: String,
    var createdAt: String,
    var checksumText: String,
    var carrier: String,
    var archived: String,
    var statusLabel: String,
)

fun toGlobalQualificationPreferenceView(item: GlobalQualificationPreference): GlobalQualificationPreferenceView {
    return GlobalQualificationPreferenceView(
        code = item.code,
        name = truncate(item.name, 120),
        createdAt = item.createdAt.toString(),
        checksumText = item.checksum,
        carrier = item.carrier,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalQualificationPreferenceViews(items: MutableList<GlobalQualificationPreference>): MutableList<GlobalQualificationPreferenceView> {
    val views = mutableListOf<GlobalQualificationPreferenceView>()
    for (item in items) {
        views.add(toGlobalQualificationPreferenceView(item))
    }
    return views
}

fun sumGlobalQualificationPreferenceView(view: GlobalQualificationPreferenceView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}
