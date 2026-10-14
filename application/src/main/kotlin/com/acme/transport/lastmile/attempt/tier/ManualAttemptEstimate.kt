package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual attempt estimate is in its life cycle. */
enum class ManualAttemptEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual attempt estimate, with what the planners need to act on it. */
data class ManualAttemptEstimate(
    var code: String,
    var label: String,
    var source: String,
    var sequence: Int,
    var amount: Double,
    var active: Boolean,
    var dock: String,
    var retries: Int,
    var fuelLiters: Double,
    var maximum: Double,
    var expiresAt: Int,
    var status: ManualAttemptEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptEstimateStatus): Boolean {
        when (status) {
            ManualAttemptEstimateStatus.PROPOSED -> return next == ManualAttemptEstimateStatus.REJECTED || next == ManualAttemptEstimateStatus.WITHDRAWN
            ManualAttemptEstimateStatus.APPROVED -> return next == ManualAttemptEstimateStatus.REJECTED || next == ManualAttemptEstimateStatus.WITHDRAWN
            ManualAttemptEstimateStatus.REJECTED -> {
                when (next) {
                    ManualAttemptEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAttemptEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual attempt estimate"} $code"

    fun netTotal(): Double = round((fuelLiters - amount) * 100) / 100

    fun snapshot(): ManualAttemptEstimate {
        return ManualAttemptEstimate(
            code = code,
            label = label,
            source = source,
            sequence = sequence,
            amount = amount,
            active = active,
            dock = dock,
            retries = retries,
            fuelLiters = fuelLiters,
            maximum = maximum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual attempt estimate in status proposed. */
fun newManualAttemptEstimate(code: String, label: String): ManualAttemptEstimate {
    return ManualAttemptEstimate(
        code = code,
        label = label,
        source = "",
        sequence = 0,
        amount = 0.0,
        active = false,
        dock = "",
        retries = 0,
        fuelLiters = 0.0,
        maximum = 0.0,
        expiresAt = 0,
        status = ManualAttemptEstimateStatus.PROPOSED,
    )
}
