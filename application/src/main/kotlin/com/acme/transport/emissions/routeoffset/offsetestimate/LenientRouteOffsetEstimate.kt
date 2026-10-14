package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient route offset estimate records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a lenient route offset estimate is in its life cycle. */
enum class LenientRouteOffsetEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient route offset estimate and its current status. */
data class LenientRouteOffsetEstimate(
    var code: String,
    var label: String,
    var capacity: Int,
    var maximum: Double,
    var segment: String,
    var reviewer: String,
    var checksum: String,
    var driverId: String,
    var status: LenientRouteOffsetEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRouteOffsetEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRouteOffsetEstimateStatus): Boolean {
        when (status) {
            LenientRouteOffsetEstimateStatus.DRAFT -> {
                when (next) {
                    LenientRouteOffsetEstimateStatus.PENDING, LenientRouteOffsetEstimateStatus.ACTIVE -> return true
                }
                return false
            }
            LenientRouteOffsetEstimateStatus.PENDING -> return next == LenientRouteOffsetEstimateStatus.ACTIVE
            LenientRouteOffsetEstimateStatus.ACTIVE -> {
                when (next) {
                    LenientRouteOffsetEstimateStatus.CLOSED -> return true
                }
                return false
            }
            LenientRouteOffsetEstimateStatus.SUSPENDED -> return next == LenientRouteOffsetEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientRouteOffsetEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient route offset estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(maximum * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientRouteOffsetEstimate {
        return LenientRouteOffsetEstimate(
            code = code,
            label = label,
            capacity = capacity,
            maximum = maximum,
            segment = segment,
            reviewer = reviewer,
            checksum = checksum,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient route offset estimate in status draft. */
fun newLenientRouteOffsetEstimate(code: String, label: String): LenientRouteOffsetEstimate {
    return LenientRouteOffsetEstimate(
        code = code,
        label = label,
        capacity = 0,
        maximum = 0.0,
        segment = "",
        reviewer = "",
        checksum = "",
        driverId = "",
        status = LenientRouteOffsetEstimateStatus.DRAFT,
    )
}
