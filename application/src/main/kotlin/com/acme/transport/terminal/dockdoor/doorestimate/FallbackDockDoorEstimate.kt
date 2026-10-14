package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback dock door estimate records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a fallback dock door estimate is in its life cycle. */
enum class FallbackDockDoorEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback dock door estimate, with what the planners need to act on it. */
data class FallbackDockDoorEstimate(
    var code: String,
    var title: String,
    var carrier: String,
    var minimum: Double,
    var endsAt: Int,
    var threshold: Double,
    var amount: Double,
    var durationMinutes: Int,
    var retries: Int,
    var loadKg: Double,
    var status: FallbackDockDoorEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDockDoorEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDockDoorEstimateStatus): Boolean {
        when (status) {
            FallbackDockDoorEstimateStatus.OPEN -> {
                when (next) {
                    FallbackDockDoorEstimateStatus.IN_REVIEW -> return true
                }
                return false
            }
            FallbackDockDoorEstimateStatus.IN_REVIEW -> return next == FallbackDockDoorEstimateStatus.REOPENED || next == FallbackDockDoorEstimateStatus.ARCHIVED
            FallbackDockDoorEstimateStatus.RESOLVED -> return next == FallbackDockDoorEstimateStatus.ARCHIVED
            FallbackDockDoorEstimateStatus.REOPENED -> {
                when (next) {
                    FallbackDockDoorEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDockDoorEstimateStatus) {
        if (!canMoveTo(next)) {
            error("fallback dock door estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback dock door estimate"} $code"

    fun projectedLoad(): Double = round(loadKg * endsAt.toDouble() * 100) / 100

    fun snapshot(): FallbackDockDoorEstimate {
        return FallbackDockDoorEstimate(
            code = code,
            title = title,
            carrier = carrier,
            minimum = minimum,
            endsAt = endsAt,
            threshold = threshold,
            amount = amount,
            durationMinutes = durationMinutes,
            retries = retries,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback dock door estimate in status open. */
fun newFallbackDockDoorEstimate(code: String, title: String): FallbackDockDoorEstimate {
    return FallbackDockDoorEstimate(
        code = code,
        title = title,
        carrier = "",
        minimum = 0.0,
        endsAt = 0,
        threshold = 0.0,
        amount = 0.0,
        durationMinutes = 0,
        retries = 0,
        loadKg = 0.0,
        status = FallbackDockDoorEstimateStatus.OPEN,
    )
}
