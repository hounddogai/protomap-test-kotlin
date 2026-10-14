package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted zone manifest is in its life cycle. */
enum class WeightedZoneManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted zone manifest. Its code is unique within a tenant. */
data class WeightedZoneManifest(
    var code: String,
    var label: String,
    var limit: Int,
    var createdAt: Int,
    var reviewer: String,
    var weight: Double,
    var status: WeightedZoneManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedZoneManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedZoneManifestStatus): Boolean {
        when (status) {
            WeightedZoneManifestStatus.IDLE -> return next == WeightedZoneManifestStatus.DRAINING || next == WeightedZoneManifestStatus.STOPPED
            WeightedZoneManifestStatus.WARMING -> return next == WeightedZoneManifestStatus.READY || next == WeightedZoneManifestStatus.STOPPED
            WeightedZoneManifestStatus.READY -> return next == WeightedZoneManifestStatus.DRAINING || next == WeightedZoneManifestStatus.STOPPED
            WeightedZoneManifestStatus.DRAINING -> {
                when (next) {
                    WeightedZoneManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedZoneManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted zone manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted zone manifest"} $code"

    fun effectiveExposure(): Double = round(limit.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedZoneManifest {
        return WeightedZoneManifest(
            code = code,
            label = label,
            limit = limit,
            createdAt = createdAt,
            reviewer = reviewer,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted zone manifest in status idle. */
fun newWeightedZoneManifest(code: String, label: String): WeightedZoneManifest {
    return WeightedZoneManifest(
        code = code,
        label = label,
        limit = 0,
        createdAt = 0,
        reviewer = "",
        weight = 0.0,
        status = WeightedZoneManifestStatus.IDLE,
    )
}
