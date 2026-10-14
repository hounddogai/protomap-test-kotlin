package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard estimate is in its life cycle. */
enum class StrictTrailerYardEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict trailer yard estimate, with what the planners need to act on it. */
data class StrictTrailerYardEstimate(
    var code: String,
    var name: String,
    var attempts: Int,
    var loadKg: Double,
    var expiresAt: Int,
    var odometer: Int,
    var depot: String,
    var priority: Int,
    var pallets: Int,
    var lane: String,
    var status: StrictTrailerYardEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardEstimateStatus): Boolean {
        when (status) {
            StrictTrailerYardEstimateStatus.PROPOSED -> {
                when (next) {
                    StrictTrailerYardEstimateStatus.APPROVED -> return true
                }
                return false
            }
            StrictTrailerYardEstimateStatus.APPROVED -> return next == StrictTrailerYardEstimateStatus.WITHDRAWN
            StrictTrailerYardEstimateStatus.REJECTED -> {
                when (next) {
                    StrictTrailerYardEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((odometer.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): StrictTrailerYardEstimate {
        return StrictTrailerYardEstimate(
            code = code,
            name = name,
            attempts = attempts,
            loadKg = loadKg,
            expiresAt = expiresAt,
            odometer = odometer,
            depot = depot,
            priority = priority,
            pallets = pallets,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict trailer yard estimate in status proposed. */
fun newStrictTrailerYardEstimate(code: String, name: String): StrictTrailerYardEstimate {
    return StrictTrailerYardEstimate(
        code = code,
        name = name,
        attempts = 0,
        loadKg = 0.0,
        expiresAt = 0,
        odometer = 0,
        depot = "",
        priority = 0,
        pallets = 0,
        lane = "",
        status = StrictTrailerYardEstimateStatus.PROPOSED,
    )
}
