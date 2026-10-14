package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary scorecard adjustment is in its life cycle. */
enum class PrimaryScorecardAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary scorecard adjustment, with what the planners need to act on it. */
data class PrimaryScorecardAdjustment(
    var code: String,
    var label: String,
    var routeCode: String,
    var odometer: Int,
    var ownerId: String,
    var version: Int,
    var expiresAt: Int,
    var startsAt: Int,
    var sequence: Int,
    var status: PrimaryScorecardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardAdjustmentStatus): Boolean {
        when (status) {
            PrimaryScorecardAdjustmentStatus.NEW -> return next == PrimaryScorecardAdjustmentStatus.QUEUED
            PrimaryScorecardAdjustmentStatus.QUEUED -> {
                when (next) {
                    PrimaryScorecardAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            PrimaryScorecardAdjustmentStatus.RUNNING -> {
                when (next) {
                    PrimaryScorecardAdjustmentStatus.DONE, PrimaryScorecardAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            PrimaryScorecardAdjustmentStatus.DONE -> return next == PrimaryScorecardAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"primary scorecard adjustment"} $code"

    fun netLoad(): Double = round((version.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryScorecardAdjustment {
        return PrimaryScorecardAdjustment(
            code = code,
            label = label,
            routeCode = routeCode,
            odometer = odometer,
            ownerId = ownerId,
            version = version,
            expiresAt = expiresAt,
            startsAt = startsAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary scorecard adjustment in status new. */
fun newPrimaryScorecardAdjustment(code: String, label: String): PrimaryScorecardAdjustment {
    return PrimaryScorecardAdjustment(
        code = code,
        label = label,
        routeCode = "",
        odometer = 0,
        ownerId = "",
        version = 0,
        expiresAt = 0,
        startsAt = 0,
        sequence = 0,
        status = PrimaryScorecardAdjustmentStatus.NEW,
    )
}
