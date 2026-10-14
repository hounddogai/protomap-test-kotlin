package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered attempt request is in its life cycle. */
enum class TieredAttemptRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One tiered attempt request, with what the planners need to act on it. */
data class TieredAttemptRequest(
    var code: String,
    var label: String,
    var routeCode: String,
    var minimum: Double,
    var stopCount: Int,
    var cost: Double,
    var discount: Double,
    var score: Double,
    var updatedAt: Int,
    var weight: Double,
    var status: TieredAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAttemptRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAttemptRequestStatus): Boolean {
        when (status) {
            TieredAttemptRequestStatus.PROPOSED -> {
                when (next) {
                    TieredAttemptRequestStatus.APPROVED, TieredAttemptRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredAttemptRequestStatus.APPROVED -> return next == TieredAttemptRequestStatus.REJECTED
            TieredAttemptRequestStatus.REJECTED -> return next == TieredAttemptRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("tiered attempt request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((updatedAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): TieredAttemptRequest {
        return TieredAttemptRequest(
            code = code,
            label = label,
            routeCode = routeCode,
            minimum = minimum,
            stopCount = stopCount,
            cost = cost,
            discount = discount,
            score = score,
            updatedAt = updatedAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered attempt request in status proposed. */
fun newTieredAttemptRequest(code: String, label: String): TieredAttemptRequest {
    return TieredAttemptRequest(
        code = code,
        label = label,
        routeCode = "",
        minimum = 0.0,
        stopCount = 0,
        cost = 0.0,
        discount = 0.0,
        score = 0.0,
        updatedAt = 0,
        weight = 0.0,
        status = TieredAttemptRequestStatus.PROPOSED,
    )
}
