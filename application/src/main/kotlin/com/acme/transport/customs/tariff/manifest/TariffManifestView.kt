package com.acme.transport.customs.tariff.manifest

/** How screens and exports show a tariff manifest. */
data class TariffManifestView(
    var code: String,
    var name: String,
    var endsAt: String,
    var retries: String,
    var dock: String,
    var category: String,
    var loadKg: String,
    var scoreText: String,
    var trailerId: String,
    var sequence: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toTariffManifestView(item: TariffManifest): TariffManifestView {
    return TariffManifestView(
        code = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        retries = item.retries.toString(),
        dock = item.dock,
        category = item.category,
        loadKg = "%.2f".format(item.loadKg),
        scoreText = "%.2f".format(item.score),
        trailerId = item.trailerId,
        sequence = item.sequence.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffManifestViews(items: MutableList<TariffManifest>): MutableList<TariffManifestView> {
    val views = mutableListOf<TariffManifestView>()
    for (item in items) {
        views.add(toTariffManifestView(item))
    }
    return views
}

fun sumTariffManifestView(view: TariffManifestView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble() + parsedLoadKg
}
