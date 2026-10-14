package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.text.normalize

// The fallback clearance variance records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a fallback clearance variance is in its life cycle. */
enum class FallbackClearanceVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback clearance variance and its current status. */
data class FallbackClearanceVariance(
    var code: String,
    var title: String,
    var depot: String,
    var archived: Boolean,
    var trailerId: String,
    var quantity: Int,
    var status: FallbackClearanceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceVarianceStatus): Boolean {
        when (status) {
            FallbackClearanceVarianceStatus.NEW -> {
                when (next) {
                    FallbackClearanceVarianceStatus.DONE, FallbackClearanceVarianceStatus.FAILED -> return true
                }
                return false
            }
            FallbackClearanceVarianceStatus.QUEUED -> return next == FallbackClearanceVarianceStatus.FAILED
            FallbackClearanceVarianceStatus.RUNNING -> return next == FallbackClearanceVarianceStatus.DONE || next == FallbackClearanceVarianceStatus.FAILED
            FallbackClearanceVarianceStatus.DONE -> return next == FallbackClearanceVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackClearanceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): FallbackClearanceVariance {
        return FallbackClearanceVariance(
            code = code,
            title = title,
            depot = depot,
            archived = archived,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback clearance variance in status new. */
fun newFallbackClearanceVariance(code: String, title: String): FallbackClearanceVariance {
    return FallbackClearanceVariance(
        code = code,
        title = title,
        depot = "",
        archived = false,
        trailerId = "",
        quantity = 0,
        status = FallbackClearanceVarianceStatus.NEW,
    )
}
