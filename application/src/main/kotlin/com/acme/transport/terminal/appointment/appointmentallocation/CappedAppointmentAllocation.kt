package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped appointment allocation records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a capped appointment allocation is in its life cycle. */
enum class CappedAppointmentAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped appointment allocation and its current status. */
data class CappedAppointmentAllocation(
    var code: String,
    var label: String,
    var notes: String,
    var distanceKm: Double,
    var batchSize: Int,
    var timezone: String,
    var priority: Int,
    var maximum: Double,
    var revision: Int,
    var endsAt: Int,
    var quantity: Int,
    var status: CappedAppointmentAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAppointmentAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAppointmentAllocationStatus): Boolean {
        when (status) {
            CappedAppointmentAllocationStatus.CREATED -> return next == CappedAppointmentAllocationStatus.VALIDATED || next == CappedAppointmentAllocationStatus.PUBLISHED
            CappedAppointmentAllocationStatus.VALIDATED -> return next == CappedAppointmentAllocationStatus.PUBLISHED || next == CappedAppointmentAllocationStatus.RETIRED
            CappedAppointmentAllocationStatus.PUBLISHED -> {
                when (next) {
                    CappedAppointmentAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAppointmentAllocationStatus) {
        if (!canMoveTo(next)) {
            error("capped appointment allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((revision.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): CappedAppointmentAllocation {
        return CappedAppointmentAllocation(
            code = code,
            label = label,
            notes = notes,
            distanceKm = distanceKm,
            batchSize = batchSize,
            timezone = timezone,
            priority = priority,
            maximum = maximum,
            revision = revision,
            endsAt = endsAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped appointment allocation in status created. */
fun newCappedAppointmentAllocation(code: String, label: String): CappedAppointmentAllocation {
    return CappedAppointmentAllocation(
        code = code,
        label = label,
        notes = "",
        distanceKm = 0.0,
        batchSize = 0,
        timezone = "",
        priority = 0,
        maximum = 0.0,
        revision = 0,
        endsAt = 0,
        quantity = 0,
        status = CappedAppointmentAllocationStatus.CREATED,
    )
}
