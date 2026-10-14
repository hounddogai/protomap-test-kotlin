package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor manifest. */
data class EmissionFactorManifestView(
    var codeText: String,
    var titleText: String,
    var sourceText: String,
    var dock: String,
    var createdAt: String,
    var batchSize: String,
    var attempts: String,
    var revision: String,
    var weight: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toEmissionFactorManifestView(item: EmissionFactorManifest): EmissionFactorManifestView {
    return EmissionFactorManifestView(
        codeText = item.code,
        titleText = item.title,
        sourceText = truncate(item.source, 80),
        dock = item.dock,
        createdAt = item.createdAt.toString(),
        batchSize = item.batchSize.toString(),
        attempts = item.attempts.toString(),
        revision = item.revision.toString(),
        weight = "%.2f".format(item.weight),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorManifestViews(items: MutableList<EmissionFactorManifest>): MutableList<EmissionFactorManifestView> {
    val views = mutableListOf<EmissionFactorManifestView>()
    for (item in items) {
        views.add(toEmissionFactorManifestView(item))
    }
    return views
}

fun sumEmissionFactorManifestView(view: EmissionFactorManifestView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedCreatedAt.toDouble() + parsedBatchSize.toDouble() + parsedAttempts.toDouble()
}
