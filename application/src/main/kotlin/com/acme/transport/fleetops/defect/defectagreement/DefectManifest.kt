package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect manifest is in its life cycle. */
enum class DefectManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One defect manifest, with what the planners need to act on it. */
data class DefectManifest(
    var code: String,
    var title: String,
    var driverId: String,
    var sequence: Int,
    var expiresAt: Int,
    var odometer: Int,
    var margin: Double,
    var loadKg: Double,
    var discount: Double,
    var checksum: String,
    var status: DefectManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectManifestStatus): Boolean {
        when (status) {
            DefectManifestStatus.IDLE -> return next == DefectManifestStatus.WARMING || next == DefectManifestStatus.DRAINING
            DefectManifestStatus.WARMING -> return next == DefectManifestStatus.READY || next == DefectManifestStatus.STOPPED
            DefectManifestStatus.READY -> return next == DefectManifestStatus.DRAINING
            DefectManifestStatus.DRAINING -> {
                when (next) {
                    DefectManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectManifestStatus) {
        if (!canMoveTo(next)) {
            error("defect manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((sequence.toDouble() - margin) * 100) / 100

    fun snapshot(): DefectManifest {
        return DefectManifest(
            code = code,
            title = title,
            driverId = driverId,
            sequence = sequence,
            expiresAt = expiresAt,
            odometer = odometer,
            margin = margin,
            loadKg = loadKg,
            discount = discount,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a defect manifest in status idle. */
fun newDefectManifest(code: String, title: String): DefectManifest {
    return DefectManifest(
        code = code,
        title = title,
        driverId = "",
        sequence = 0,
        expiresAt = 0,
        odometer = 0,
        margin = 0.0,
        loadKg = 0.0,
        discount = 0.0,
        checksum = "",
        status = DefectManifestStatus.IDLE,
    )
}
