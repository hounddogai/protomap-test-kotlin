package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor manifest is in its life cycle. */
enum class EmissionFactorManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A emission factor manifest. Its code is unique within a tenant. */
data class EmissionFactorManifest(
    var code: String,
    var title: String,
    var source: String,
    var dock: String,
    var createdAt: Int,
    var batchSize: Int,
    var attempts: Int,
    var revision: Int,
    var weight: Double,
    var archived: Boolean,
    var status: EmissionFactorManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorManifestStatus): Boolean {
        when (status) {
            EmissionFactorManifestStatus.IDLE -> return next == EmissionFactorManifestStatus.READY
            EmissionFactorManifestStatus.WARMING -> return next == EmissionFactorManifestStatus.READY
            EmissionFactorManifestStatus.READY -> return next == EmissionFactorManifestStatus.DRAINING
            EmissionFactorManifestStatus.DRAINING -> return next == EmissionFactorManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: EmissionFactorManifestStatus) {
        if (!canMoveTo(next)) {
            error("emission factor manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((batchSize.toDouble() + weight) * 100) / 100

    fun snapshot(): EmissionFactorManifest {
        return EmissionFactorManifest(
            code = code,
            title = title,
            source = source,
            dock = dock,
            createdAt = createdAt,
            batchSize = batchSize,
            attempts = attempts,
            revision = revision,
            weight = weight,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a emission factor manifest in status idle. */
fun newEmissionFactorManifest(code: String, title: String): EmissionFactorManifest {
    return EmissionFactorManifest(
        code = code,
        title = title,
        source = "",
        dock = "",
        createdAt = 0,
        batchSize = 0,
        attempts = 0,
        revision = 0,
        weight = 0.0,
        archived = false,
        status = EmissionFactorManifestStatus.IDLE,
    )
}
