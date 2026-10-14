package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback defect variance is in its life cycle. */
enum class FallbackDefectVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback defect variance as the defect team keeps it. */
data class FallbackDefectVariance(
    var code: String,
    var label: String,
    var reviewer: String,
    var driverId: String,
    var depot: String,
    var batchSize: Int,
    var capacity: Int,
    var graceDays: Int,
    var attempts: Int,
    var contactEmail: String,
    var status: FallbackDefectVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDefectVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDefectVarianceStatus): Boolean {
        when (status) {
            FallbackDefectVarianceStatus.NEW -> {
                when (next) {
                    FallbackDefectVarianceStatus.QUEUED -> return true
                }
                return false
            }
            FallbackDefectVarianceStatus.QUEUED -> {
                when (next) {
                    FallbackDefectVarianceStatus.DONE, FallbackDefectVarianceStatus.FAILED -> return true
                }
                return false
            }
            FallbackDefectVarianceStatus.RUNNING -> {
                when (next) {
                    FallbackDefectVarianceStatus.DONE -> return true
                }
                return false
            }
            FallbackDefectVarianceStatus.DONE -> {
                when (next) {
                    FallbackDefectVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDefectVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback defect variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback defect variance"} $code"

    fun netVolume(): Double = round((graceDays.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): FallbackDefectVariance {
        return FallbackDefectVariance(
            code = code,
            label = label,
            reviewer = reviewer,
            driverId = driverId,
            depot = depot,
            batchSize = batchSize,
            capacity = capacity,
            graceDays = graceDays,
            attempts = attempts,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback defect variance in status new. */
fun newFallbackDefectVariance(code: String, label: String): FallbackDefectVariance {
    return FallbackDefectVariance(
        code = code,
        label = label,
        reviewer = "",
        driverId = "",
        depot = "",
        batchSize = 0,
        capacity = 0,
        graceDays = 0,
        attempts = 0,
        contactEmail = "",
        status = FallbackDefectVarianceStatus.NEW,
    )
}
