package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer variance is in its life cycle. */
enum class ManualTrailerVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual trailer variance, with what the planners need to act on it. */
data class ManualTrailerVariance(
    var code: String,
    var name: String,
    var priority: Int,
    var archived: Boolean,
    var quantity: Int,
    var budget: Double,
    var status: ManualTrailerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerVarianceStatus): Boolean {
        when (status) {
            ManualTrailerVarianceStatus.PLANNED -> {
                when (next) {
                    ManualTrailerVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualTrailerVarianceStatus.SCHEDULED -> {
                when (next) {
                    ManualTrailerVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            ManualTrailerVarianceStatus.IN_PROGRESS -> return next == ManualTrailerVarianceStatus.CANCELLED
            ManualTrailerVarianceStatus.COMPLETED -> return next == ManualTrailerVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualTrailerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round((quantity.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): ManualTrailerVariance {
        return ManualTrailerVariance(
            code = code,
            name = name,
            priority = priority,
            archived = archived,
            quantity = quantity,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual trailer variance in status planned. */
fun newManualTrailerVariance(code: String, name: String): ManualTrailerVariance {
    return ManualTrailerVariance(
        code = code,
        name = name,
        priority = 0,
        archived = false,
        quantity = 0,
        budget = 0.0,
        status = ManualTrailerVarianceStatus.PLANNED,
    )
}
