package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize

/** Where a trailer yard variance is in its life cycle. */
enum class TrailerYardVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer yard variance as the trailer yard team keeps it. */
data class TrailerYardVariance(
    var code: String,
    var name: String,
    var routeCode: String,
    var retries: Int,
    var title: String,
    var status: TrailerYardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardVarianceStatus): Boolean {
        when (status) {
            TrailerYardVarianceStatus.OPEN -> {
                when (next) {
                    TrailerYardVarianceStatus.REOPENED -> return true
                }
                return false
            }
            TrailerYardVarianceStatus.IN_REVIEW -> {
                when (next) {
                    TrailerYardVarianceStatus.REOPENED, TrailerYardVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerYardVarianceStatus.RESOLVED -> return next == TrailerYardVarianceStatus.REOPENED || next == TrailerYardVarianceStatus.ARCHIVED
            TrailerYardVarianceStatus.REOPENED -> return next == TrailerYardVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerYardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard variance"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): TrailerYardVariance {
        return TrailerYardVariance(
            code = code,
            name = name,
            routeCode = routeCode,
            retries = retries,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard variance in status open. */
fun newTrailerYardVariance(code: String, name: String): TrailerYardVariance {
    return TrailerYardVariance(
        code = code,
        name = name,
        routeCode = "",
        retries = 0,
        title = "",
        status = TrailerYardVarianceStatus.OPEN,
    )
}
