package com.acme.transport.linehaul.laneplan.manifest

/** How screens and exports show a legacy lane plan manifest. */
data class LegacyLanePlanManifestView(
    var code: String,
    var label: String,
    var loadKg: String,
    var batchSize: String,
    var timezone: String,
    var revision: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toLegacyLanePlanManifestView(item: LegacyLanePlanManifest): LegacyLanePlanManifestView {
    return LegacyLanePlanManifestView(
        code = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        batchSize = item.batchSize.toString(),
        timezone = item.timezone,
        revision = item.revision.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanManifestViews(items: MutableList<LegacyLanePlanManifest>): MutableList<LegacyLanePlanManifestView> {
    val views = mutableListOf<LegacyLanePlanManifestView>()
    for (item in items) {
        views.add(toLegacyLanePlanManifestView(item))
    }
    return views
}

fun sumLegacyLanePlanManifestView(view: LegacyLanePlanManifestView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedLoadKg + parsedBatchSize.toDouble() + parsedRevision.toDouble()
}
