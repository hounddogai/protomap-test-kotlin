package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback accessorial score is in its life cycle. */
enum class FallbackAccessorialScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback accessorial score as the accessorial team keeps it. */
data class FallbackAccessorialScore(
    var code: String,
    var label: String,
    var active: Boolean,
    var score: Double,
    var pallets: Int,
    var updatedAt: Int,
    var title: String,
    var sequence: Int,
    var attempts: Int,
    var depot: String,
    var priority: Int,
    var status: FallbackAccessorialScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialScoreStatus): Boolean {
        when (status) {
            FallbackAccessorialScoreStatus.IDLE -> return next == FallbackAccessorialScoreStatus.READY
            FallbackAccessorialScoreStatus.WARMING -> {
                when (next) {
                    FallbackAccessorialScoreStatus.STOPPED -> return true
                }
                return false
            }
            FallbackAccessorialScoreStatus.READY -> {
                when (next) {
                    FallbackAccessorialScoreStatus.STOPPED -> return true
                }
                return false
            }
            FallbackAccessorialScoreStatus.DRAINING -> {
                when (next) {
                    FallbackAccessorialScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialScoreStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((attempts.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): FallbackAccessorialScore {
        return FallbackAccessorialScore(
            code = code,
            label = label,
            active = active,
            score = score,
            pallets = pallets,
            updatedAt = updatedAt,
            title = title,
            sequence = sequence,
            attempts = attempts,
            depot = depot,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback accessorial score in status idle. */
fun newFallbackAccessorialScore(code: String, label: String): FallbackAccessorialScore {
    return FallbackAccessorialScore(
        code = code,
        label = label,
        active = false,
        score = 0.0,
        pallets = 0,
        updatedAt = 0,
        title = "",
        sequence = 0,
        attempts = 0,
        depot = "",
        priority = 0,
        status = FallbackAccessorialScoreStatus.IDLE,
    )
}
