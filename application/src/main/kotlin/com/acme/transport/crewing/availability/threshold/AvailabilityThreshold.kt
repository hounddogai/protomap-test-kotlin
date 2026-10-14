package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability threshold records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability threshold is in its life cycle. */
enum class AvailabilityThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A availability threshold. Its code is unique within a tenant. */
data class AvailabilityThreshold(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var country: String,
    var channel: String,
    var dock: String,
    var reviewer: String,
    var archived: Boolean,
    var startsAt: Int,
    var etaMinutes: Int,
    var priority: Int,
    var status: AvailabilityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityThresholdStatus): Boolean {
        when (status) {
            AvailabilityThresholdStatus.DRAFT -> return next == AvailabilityThresholdStatus.PENDING || next == AvailabilityThresholdStatus.SUSPENDED
            AvailabilityThresholdStatus.PENDING -> {
                when (next) {
                    AvailabilityThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            AvailabilityThresholdStatus.ACTIVE -> return next == AvailabilityThresholdStatus.SUSPENDED
            AvailabilityThresholdStatus.SUSPENDED -> return next == AvailabilityThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AvailabilityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("availability threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((etaMinutes.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityThreshold {
        return AvailabilityThreshold(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            country = country,
            channel = channel,
            dock = dock,
            reviewer = reviewer,
            archived = archived,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a availability threshold in status draft. */
fun newAvailabilityThreshold(code: String, name: String): AvailabilityThreshold {
    return AvailabilityThreshold(
        code = code,
        name = name,
        fuelLiters = 0.0,
        country = "",
        channel = "",
        dock = "",
        reviewer = "",
        archived = false,
        startsAt = 0,
        etaMinutes = 0,
        priority = 0,
        status = AvailabilityThresholdStatus.DRAFT,
    )
}
