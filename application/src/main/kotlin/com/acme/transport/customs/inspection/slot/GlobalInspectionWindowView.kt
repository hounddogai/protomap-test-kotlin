package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection window. */
data class GlobalInspectionWindowView(
    var code: String,
    var title: String,
    var ownerId: String,
    var versionText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toGlobalInspectionWindowView(item: GlobalInspectionWindow): GlobalInspectionWindowView {
    return GlobalInspectionWindowView(
        code = item.code,
        title = item.title,
        ownerId = item.ownerId,
        versionText = item.version.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionWindowViews(items: MutableList<GlobalInspectionWindow>): MutableList<GlobalInspectionWindowView> {
    val views = mutableListOf<GlobalInspectionWindowView>()
    for (item in items) {
        views.add(toGlobalInspectionWindowView(item))
    }
    return views
}

fun sumGlobalInspectionWindowView(view: GlobalInspectionWindowView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
