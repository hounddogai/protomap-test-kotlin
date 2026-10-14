package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling declaration manifest. */
data class RollingDeclarationManifestView(
    var code: String,
    var nameText: String,
    var attemptsText: String,
    var floorValueText: String,
    var revision: String,
    var cost: String,
    var summary: String,
    var weightText: String,
    var shiftCode: String,
    var category: String,
    var lane: String,
    var statusLabel: String,
)

fun toRollingDeclarationManifestView(item: RollingDeclarationManifest): RollingDeclarationManifestView {
    return RollingDeclarationManifestView(
        code = item.code,
        nameText = item.name,
        attemptsText = item.attempts.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        revision = item.revision.toString(),
        cost = "%.2f".format(item.cost),
        summary = item.summary,
        weightText = "%.2f".format(item.weight),
        shiftCode = truncate(item.shiftCode, 120),
        category = truncate(item.category, 80),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDeclarationManifestViews(items: MutableList<RollingDeclarationManifest>): MutableList<RollingDeclarationManifestView> {
    val views = mutableListOf<RollingDeclarationManifestView>()
    for (item in items) {
        views.add(toRollingDeclarationManifestView(item))
    }
    return views
}

fun sumRollingDeclarationManifestView(view: RollingDeclarationManifestView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedAttempts.toDouble() + parsedFloorValue + parsedRevision.toDouble()
}
