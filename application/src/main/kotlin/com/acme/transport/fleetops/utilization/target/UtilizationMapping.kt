package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization mapping is in its life cycle. */
enum class UtilizationMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One utilization mapping, with what the planners need to act on it. */
data class UtilizationMapping(
    var code: String,
    var label: String,
    var shiftCode: String,
    var pinned: Boolean,
    var pallets: Int,
    var startsAt: Int,
    var channel: String,
    var rate: Double,
    var status: UtilizationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationMappingStatus): Boolean {
        when (status) {
            UtilizationMappingStatus.DRAFT -> return next == UtilizationMappingStatus.ACTIVE || next == UtilizationMappingStatus.CLOSED
            UtilizationMappingStatus.PENDING -> {
                when (next) {
                    UtilizationMappingStatus.ACTIVE, UtilizationMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            UtilizationMappingStatus.ACTIVE -> return next == UtilizationMappingStatus.SUSPENDED || next == UtilizationMappingStatus.CLOSED
            UtilizationMappingStatus.SUSPENDED -> {
                when (next) {
                    UtilizationMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationMappingStatus) {
        if (!canMoveTo(next)) {
            error("utilization mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"utilization mapping"} $code"

    fun adjustedExposure(): Double = round(startsAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): UtilizationMapping {
        return UtilizationMapping(
            code = code,
            label = label,
            shiftCode = shiftCode,
            pinned = pinned,
            pallets = pallets,
            startsAt = startsAt,
            channel = channel,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a utilization mapping in status draft. */
fun newUtilizationMapping(code: String, label: String): UtilizationMapping {
    return UtilizationMapping(
        code = code,
        label = label,
        shiftCode = "",
        pinned = false,
        pallets = 0,
        startsAt = 0,
        channel = "",
        rate = 0.0,
        status = UtilizationMappingStatus.DRAFT,
    )
}
