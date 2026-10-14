package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted defect manifest is in its life cycle. */
enum class WeightedDefectManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted defect manifest. Its code is unique within a tenant. */
data class WeightedDefectManifest(
    var code: String,
    var label: String,
    var threshold: Double,
    var etaMinutes: Int,
    var pallets: Int,
    var dock: String,
    var notes: String,
    var quantity: Int,
    var distanceKm: Double,
    var checksum: String,
    var expiresAt: Int,
    var status: WeightedDefectManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDefectManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDefectManifestStatus): Boolean {
        when (status) {
            WeightedDefectManifestStatus.DRAFT -> {
                when (next) {
                    WeightedDefectManifestStatus.PENDING, WeightedDefectManifestStatus.CLOSED -> return true
                }
                return false
            }
            WeightedDefectManifestStatus.PENDING -> {
                when (next) {
                    WeightedDefectManifestStatus.ACTIVE, WeightedDefectManifestStatus.CLOSED -> return true
                }
                return false
            }
            WeightedDefectManifestStatus.ACTIVE -> {
                when (next) {
                    WeightedDefectManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedDefectManifestStatus.SUSPENDED -> {
                when (next) {
                    WeightedDefectManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDefectManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted defect manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted defect manifest"} $code"

    fun weightedValue(): Double = round(quantity.toDouble() * distanceKm * 100) / 100

    fun snapshot(): WeightedDefectManifest {
        return WeightedDefectManifest(
            code = code,
            label = label,
            threshold = threshold,
            etaMinutes = etaMinutes,
            pallets = pallets,
            dock = dock,
            notes = notes,
            quantity = quantity,
            distanceKm = distanceKm,
            checksum = checksum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted defect manifest in status draft. */
fun newWeightedDefectManifest(code: String, label: String): WeightedDefectManifest {
    return WeightedDefectManifest(
        code = code,
        label = label,
        threshold = 0.0,
        etaMinutes = 0,
        pallets = 0,
        dock = "",
        notes = "",
        quantity = 0,
        distanceKm = 0.0,
        checksum = "",
        expiresAt = 0,
        status = WeightedDefectManifestStatus.DRAFT,
    )
}
