package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient availability allocation is in its life cycle. */
enum class LenientAvailabilityAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient availability allocation, with what the planners need to act on it. */
data class LenientAvailabilityAllocation(
    var code: String,
    var title: String,
    var threshold: Double,
    var sequence: Int,
    var budget: Double,
    var floorValue: Double,
    var windowDays: Int,
    var label: String,
    var distanceKm: Double,
    var etaMinutes: Int,
    var status: LenientAvailabilityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAvailabilityAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAvailabilityAllocationStatus): Boolean {
        when (status) {
            LenientAvailabilityAllocationStatus.DRAFT -> {
                when (next) {
                    LenientAvailabilityAllocationStatus.CLOSED -> return true
                }
                return false
            }
            LenientAvailabilityAllocationStatus.PENDING -> return next == LenientAvailabilityAllocationStatus.ACTIVE || next == LenientAvailabilityAllocationStatus.CLOSED
            LenientAvailabilityAllocationStatus.ACTIVE -> {
                when (next) {
                    LenientAvailabilityAllocationStatus.SUSPENDED, LenientAvailabilityAllocationStatus.CLOSED -> return true
                }
                return false
            }
            LenientAvailabilityAllocationStatus.SUSPENDED -> return next == LenientAvailabilityAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientAvailabilityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lenient availability allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedValue(): Double = round((distanceKm + budget) * 100) / 100

    fun snapshot(): LenientAvailabilityAllocation {
        return LenientAvailabilityAllocation(
            code = code,
            title = title,
            threshold = threshold,
            sequence = sequence,
            budget = budget,
            floorValue = floorValue,
            windowDays = windowDays,
            label = label,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient availability allocation in status draft. */
fun newLenientAvailabilityAllocation(code: String, title: String): LenientAvailabilityAllocation {
    return LenientAvailabilityAllocation(
        code = code,
        title = title,
        threshold = 0.0,
        sequence = 0,
        budget = 0.0,
        floorValue = 0.0,
        windowDays = 0,
        label = "",
        distanceKm = 0.0,
        etaMinutes = 0,
        status = LenientAvailabilityAllocationStatus.DRAFT,
    )
}
