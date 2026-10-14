package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rest break variance is in its life cycle. */
enum class DefaultRestBreakVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default rest break variance and its current status. */
data class DefaultRestBreakVariance(
    var code: String,
    var title: String,
    var odometer: Int,
    var tags: MutableList<String>,
    var notes: String,
    var pinned: Boolean,
    var stopCount: Int,
    var depot: String,
    var amount: Double,
    var batchSize: Int,
    var fuelLiters: Double,
    var status: DefaultRestBreakVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRestBreakVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRestBreakVarianceStatus): Boolean {
        when (status) {
            DefaultRestBreakVarianceStatus.OPEN -> {
                when (next) {
                    DefaultRestBreakVarianceStatus.RESOLVED, DefaultRestBreakVarianceStatus.REOPENED -> return true
                }
                return false
            }
            DefaultRestBreakVarianceStatus.IN_REVIEW -> return next == DefaultRestBreakVarianceStatus.RESOLVED || next == DefaultRestBreakVarianceStatus.REOPENED
            DefaultRestBreakVarianceStatus.RESOLVED -> return next == DefaultRestBreakVarianceStatus.REOPENED
            DefaultRestBreakVarianceStatus.REOPENED -> return next == DefaultRestBreakVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultRestBreakVarianceStatus) {
        if (!canMoveTo(next)) {
            error("default rest break variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((stopCount.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultRestBreakVariance {
        return DefaultRestBreakVariance(
            code = code,
            title = title,
            odometer = odometer,
            tags = tags,
            notes = notes,
            pinned = pinned,
            stopCount = stopCount,
            depot = depot,
            amount = amount,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default rest break variance in status open. */
fun newDefaultRestBreakVariance(code: String, title: String): DefaultRestBreakVariance {
    return DefaultRestBreakVariance(
        code = code,
        title = title,
        odometer = 0,
        tags = mutableListOf<String>(),
        notes = "",
        pinned = false,
        stopCount = 0,
        depot = "",
        amount = 0.0,
        batchSize = 0,
        fuelLiters = 0.0,
        status = DefaultRestBreakVarianceStatus.OPEN,
    )
}
