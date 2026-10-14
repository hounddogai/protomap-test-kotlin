package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy utilization slot is in its life cycle. */
enum class LegacyUtilizationSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy utilization slot. Its code is unique within a tenant. */
data class LegacyUtilizationSlot(
    var code: String,
    var label: String,
    var externalRef: String,
    var rate: Double,
    var attempts: Int,
    var status: LegacyUtilizationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationSlotStatus): Boolean {
        when (status) {
            LegacyUtilizationSlotStatus.IDLE -> {
                when (next) {
                    LegacyUtilizationSlotStatus.WARMING -> return true
                }
                return false
            }
            LegacyUtilizationSlotStatus.WARMING -> {
                when (next) {
                    LegacyUtilizationSlotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyUtilizationSlotStatus.READY -> {
                when (next) {
                    LegacyUtilizationSlotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyUtilizationSlotStatus.DRAINING -> {
                when (next) {
                    LegacyUtilizationSlotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round(rate * attempts.toDouble() * 100) / 100

    fun snapshot(): LegacyUtilizationSlot {
        return LegacyUtilizationSlot(
            code = code,
            label = label,
            externalRef = externalRef,
            rate = rate,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy utilization slot in status idle. */
fun newLegacyUtilizationSlot(code: String, label: String): LegacyUtilizationSlot {
    return LegacyUtilizationSlot(
        code = code,
        label = label,
        externalRef = "",
        rate = 0.0,
        attempts = 0,
        status = LegacyUtilizationSlotStatus.IDLE,
    )
}
