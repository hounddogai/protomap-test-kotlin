package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled availability segment records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a scheduled availability segment is in its life cycle. */
enum class ScheduledAvailabilitySegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled availability segment. Its code is unique within a tenant. */
data class ScheduledAvailabilitySegment(
    var code: String,
    var label: String,
    var driverId: String,
    var fuelLiters: Double,
    var pinned: Boolean,
    var windowDays: Int,
    var name: String,
    var trailerId: String,
    var status: ScheduledAvailabilitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAvailabilitySegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAvailabilitySegmentStatus): Boolean {
        when (status) {
            ScheduledAvailabilitySegmentStatus.DRAFT -> {
                when (next) {
                    ScheduledAvailabilitySegmentStatus.ACTIVE, ScheduledAvailabilitySegmentStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledAvailabilitySegmentStatus.PENDING -> return next == ScheduledAvailabilitySegmentStatus.CLOSED
            ScheduledAvailabilitySegmentStatus.ACTIVE -> return next == ScheduledAvailabilitySegmentStatus.SUSPENDED || next == ScheduledAvailabilitySegmentStatus.CLOSED
            ScheduledAvailabilitySegmentStatus.SUSPENDED -> return next == ScheduledAvailabilitySegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledAvailabilitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled availability segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled availability segment"} $code"

    fun weightedTotal(): Double = round(fuelLiters * windowDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledAvailabilitySegment {
        return ScheduledAvailabilitySegment(
            code = code,
            label = label,
            driverId = driverId,
            fuelLiters = fuelLiters,
            pinned = pinned,
            windowDays = windowDays,
            name = name,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled availability segment in status draft. */
fun newScheduledAvailabilitySegment(code: String, label: String): ScheduledAvailabilitySegment {
    return ScheduledAvailabilitySegment(
        code = code,
        label = label,
        driverId = "",
        fuelLiters = 0.0,
        pinned = false,
        windowDays = 0,
        name = "",
        trailerId = "",
        status = ScheduledAvailabilitySegmentStatus.DRAFT,
    )
}
