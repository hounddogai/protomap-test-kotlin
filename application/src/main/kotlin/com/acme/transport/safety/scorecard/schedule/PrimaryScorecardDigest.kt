package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary scorecard digest is in its life cycle. */
enum class PrimaryScorecardDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary scorecard digest, with what the planners need to act on it. */
data class PrimaryScorecardDigest(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var lane: String,
    var currency: String,
    var etaMinutes: Int,
    var odometer: Int,
    var margin: Double,
    var status: PrimaryScorecardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardDigestStatus): Boolean {
        when (status) {
            PrimaryScorecardDigestStatus.OPEN -> {
                when (next) {
                    PrimaryScorecardDigestStatus.IN_REVIEW, PrimaryScorecardDigestStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryScorecardDigestStatus.IN_REVIEW -> return next == PrimaryScorecardDigestStatus.RESOLVED || next == PrimaryScorecardDigestStatus.ARCHIVED
            PrimaryScorecardDigestStatus.RESOLVED -> {
                when (next) {
                    PrimaryScorecardDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryScorecardDigestStatus.REOPENED -> return next == PrimaryScorecardDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round(margin * odometer.toDouble() * 100) / 100

    fun snapshot(): PrimaryScorecardDigest {
        return PrimaryScorecardDigest(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            lane = lane,
            currency = currency,
            etaMinutes = etaMinutes,
            odometer = odometer,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary scorecard digest in status open. */
fun newPrimaryScorecardDigest(code: String, title: String): PrimaryScorecardDigest {
    return PrimaryScorecardDigest(
        code = code,
        title = title,
        fuelLiters = 0.0,
        lane = "",
        currency = "",
        etaMinutes = 0,
        odometer = 0,
        margin = 0.0,
        status = PrimaryScorecardDigestStatus.OPEN,
    )
}
