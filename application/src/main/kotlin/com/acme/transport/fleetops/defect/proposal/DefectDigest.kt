package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect digest is in its life cycle. */
enum class DefectDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One defect digest, with what the planners need to act on it. */
data class DefectDigest(
    var code: String,
    var label: String,
    var odometer: Int,
    var ratio: Double,
    var startsAt: Int,
    var status: DefectDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectDigestStatus): Boolean {
        when (status) {
            DefectDigestStatus.OPEN -> return next == DefectDigestStatus.RESOLVED
            DefectDigestStatus.IN_REVIEW -> {
                when (next) {
                    DefectDigestStatus.REOPENED, DefectDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            DefectDigestStatus.RESOLVED -> {
                when (next) {
                    DefectDigestStatus.REOPENED -> return true
                }
                return false
            }
            DefectDigestStatus.REOPENED -> {
                when (next) {
                    DefectDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectDigestStatus) {
        if (!canMoveTo(next)) {
            error("defect digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(odometer.toDouble() * ratio * 100) / 100

    fun snapshot(): DefectDigest {
        return DefectDigest(
            code = code,
            label = label,
            odometer = odometer,
            ratio = ratio,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect digest in status open. */
fun newDefectDigest(code: String, label: String): DefectDigest {
    return DefectDigest(
        code = code,
        label = label,
        odometer = 0,
        ratio = 0.0,
        startsAt = 0,
        status = DefectDigestStatus.OPEN,
    )
}
