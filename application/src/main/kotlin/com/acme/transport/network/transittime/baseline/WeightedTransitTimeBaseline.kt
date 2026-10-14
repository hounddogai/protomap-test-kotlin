package com.acme.transport.network.transittime.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted transit time baseline records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a weighted transit time baseline is in its life cycle. */
enum class WeightedTransitTimeBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted transit time baseline. Its code is unique within a tenant. */
data class WeightedTransitTimeBaseline(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var priority: Int,
    var quantity: Int,
    var limit: Int,
    var segment: String,
    var expiresAt: Int,
    var contactEmail: String,
    var amount: Double,
    var loadKg: Double,
    var status: WeightedTransitTimeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTransitTimeBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTransitTimeBaselineStatus): Boolean {
        when (status) {
            WeightedTransitTimeBaselineStatus.PLANNED -> return next == WeightedTransitTimeBaselineStatus.SCHEDULED
            WeightedTransitTimeBaselineStatus.SCHEDULED -> {
                when (next) {
                    WeightedTransitTimeBaselineStatus.IN_PROGRESS, WeightedTransitTimeBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedTransitTimeBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedTransitTimeBaselineStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedTransitTimeBaselineStatus.COMPLETED -> return next == WeightedTransitTimeBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedTransitTimeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("weighted transit time baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((priority.toDouble() + amount) * 100) / 100

    fun snapshot(): WeightedTransitTimeBaseline {
        return WeightedTransitTimeBaseline(
            code = code,
            title = title,
            pinned = pinned,
            priority = priority,
            quantity = quantity,
            limit = limit,
            segment = segment,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            amount = amount,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted transit time baseline in status planned. */
fun newWeightedTransitTimeBaseline(code: String, title: String): WeightedTransitTimeBaseline {
    return WeightedTransitTimeBaseline(
        code = code,
        title = title,
        pinned = false,
        priority = 0,
        quantity = 0,
        limit = 0,
        segment = "",
        expiresAt = 0,
        contactEmail = "",
        amount = 0.0,
        loadKg = 0.0,
        status = WeightedTransitTimeBaselineStatus.PLANNED,
    )
}
