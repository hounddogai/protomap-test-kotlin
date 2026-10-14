package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold mapping is in its life cycle. */
enum class PendingHoldMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending hold mapping. Its code is unique within a tenant. */
data class PendingHoldMapping(
    var code: String,
    var label: String,
    var approved: Boolean,
    var sequence: Int,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var status: PendingHoldMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldMappingStatus): Boolean {
        when (status) {
            PendingHoldMappingStatus.CREATED -> {
                when (next) {
                    PendingHoldMappingStatus.PUBLISHED, PendingHoldMappingStatus.RETIRED -> return true
                }
                return false
            }
            PendingHoldMappingStatus.VALIDATED -> return next == PendingHoldMappingStatus.RETIRED
            PendingHoldMappingStatus.PUBLISHED -> {
                when (next) {
                    PendingHoldMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHoldMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending hold mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): PendingHoldMapping {
        return PendingHoldMapping(
            code = code,
            label = label,
            approved = approved,
            sequence = sequence,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending hold mapping in status created. */
fun newPendingHoldMapping(code: String, label: String): PendingHoldMapping {
    return PendingHoldMapping(
        code = code,
        label = label,
        approved = false,
        sequence = 0,
        durationMinutes = 0,
        fuelLiters = 0.0,
        status = PendingHoldMappingStatus.CREATED,
    )
}
