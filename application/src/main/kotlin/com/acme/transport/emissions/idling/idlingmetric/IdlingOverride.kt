package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling override is in its life cycle. */
enum class IdlingOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a idling override and its current status. */
data class IdlingOverride(
    var code: String,
    var title: String,
    var carrier: String,
    var segment: String,
    var routeCode: String,
    var label: String,
    var checksum: String,
    var pallets: Int,
    var floorValue: Double,
    var sequence: Int,
    var discount: Double,
    var status: IdlingOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingOverrideStatus): Boolean {
        when (status) {
            IdlingOverrideStatus.PLANNED -> {
                when (next) {
                    IdlingOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingOverrideStatus.SCHEDULED -> {
                when (next) {
                    IdlingOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            IdlingOverrideStatus.IN_PROGRESS -> {
                when (next) {
                    IdlingOverrideStatus.COMPLETED, IdlingOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingOverrideStatus.COMPLETED -> {
                when (next) {
                    IdlingOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingOverrideStatus) {
        if (!canMoveTo(next)) {
            error("idling override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun projectedVolume(): Double = round(sequence.toDouble() * floorValue * 100) / 100

    fun snapshot(): IdlingOverride {
        return IdlingOverride(
            code = code,
            title = title,
            carrier = carrier,
            segment = segment,
            routeCode = routeCode,
            label = label,
            checksum = checksum,
            pallets = pallets,
            floorValue = floorValue,
            sequence = sequence,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a idling override in status planned. */
fun newIdlingOverride(code: String, title: String): IdlingOverride {
    return IdlingOverride(
        code = code,
        title = title,
        carrier = "",
        segment = "",
        routeCode = "",
        label = "",
        checksum = "",
        pallets = 0,
        floorValue = 0.0,
        sequence = 0,
        discount = 0.0,
        status = IdlingOverrideStatus.PLANNED,
    )
}
