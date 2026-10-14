package com.acme.transport.customs.declaration.declarationmanifest

/** How screens and exports show a legacy declaration manifest. */
data class LegacyDeclarationManifestView(
    var code: String,
    var labelText: String,
    var ceiling: String,
    var margin: String,
    var revisionText: String,
    var updatedAt: String,
    var channel: String,
    var minimumText: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toLegacyDeclarationManifestView(item: LegacyDeclarationManifest): LegacyDeclarationManifestView {
    return LegacyDeclarationManifestView(
        code = item.code,
        labelText = item.label,
        ceiling = "%.2f".format(item.ceiling),
        margin = "%.2f".format(item.margin),
        revisionText = item.revision.toString(),
        updatedAt = item.updatedAt.toString(),
        channel = item.channel,
        minimumText = "%.2f".format(item.minimum),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDeclarationManifestViews(items: MutableList<LegacyDeclarationManifest>): MutableList<LegacyDeclarationManifestView> {
    val views = mutableListOf<LegacyDeclarationManifestView>()
    for (item in items) {
        views.add(toLegacyDeclarationManifestView(item))
    }
    return views
}

fun sumLegacyDeclarationManifestView(view: LegacyDeclarationManifestView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedCeiling + parsedMargin + parsedRevision.toDouble()
}
