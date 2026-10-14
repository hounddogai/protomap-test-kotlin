package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled capacity allocation records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a scheduled capacity allocation is in its life cycle. */
enum class ScheduledCapacityAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled capacity allocation and its current status. */
data class ScheduledCapacityAllocation(
    var code: String,
    var label: String,
    var driverId: String,
    var archived: Boolean,
    var summary: String,
    var durationMinutes: Int,
    var revision: Int,
    var externalRef: String,
    var active: Boolean,
    var currency: String,
    var checksum: String,
    var status: ScheduledCapacityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCapacityAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCapacityAllocationStatus): Boolean {
        when (status) {
            ScheduledCapacityAllocationStatus.CREATED -> return next == ScheduledCapacityAllocationStatus.VALIDATED || next == ScheduledCapacityAllocationStatus.RETIRED
            ScheduledCapacityAllocationStatus.VALIDATED -> {
                when (next) {
                    ScheduledCapacityAllocationStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledCapacityAllocationStatus.PUBLISHED -> {
                when (next) {
                    ScheduledCapacityAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCapacityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled capacity allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((durationMinutes.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCapacityAllocation {
        return ScheduledCapacityAllocation(
            code = code,
            label = label,
            driverId = driverId,
            archived = archived,
            summary = summary,
            durationMinutes = durationMinutes,
            revision = revision,
            externalRef = externalRef,
            active = active,
            currency = currency,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled capacity allocation in status created. */
fun newScheduledCapacityAllocation(code: String, label: String): ScheduledCapacityAllocation {
    return ScheduledCapacityAllocation(
        code = code,
        label = label,
        driverId = "",
        archived = false,
        summary = "",
        durationMinutes = 0,
        revision = 0,
        externalRef = "",
        active = false,
        currency = "",
        checksum = "",
        status = ScheduledCapacityAllocationStatus.CREATED,
    )
}
