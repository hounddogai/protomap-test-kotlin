package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global utilization mapping is in its life cycle. */
enum class GlobalUtilizationMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global utilization mapping as the utilization team keeps it. */
data class GlobalUtilizationMapping(
    var code: String,
    var name: String,
    var amount: Double,
    var discount: Double,
    var approved: Boolean,
    var status: GlobalUtilizationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationMappingStatus): Boolean {
        when (status) {
            GlobalUtilizationMappingStatus.IDLE -> return next == GlobalUtilizationMappingStatus.WARMING || next == GlobalUtilizationMappingStatus.STOPPED
            GlobalUtilizationMappingStatus.WARMING -> return next == GlobalUtilizationMappingStatus.DRAINING || next == GlobalUtilizationMappingStatus.STOPPED
            GlobalUtilizationMappingStatus.READY -> {
                when (next) {
                    GlobalUtilizationMappingStatus.DRAINING, GlobalUtilizationMappingStatus.STOPPED -> return true
                }
                return false
            }
            GlobalUtilizationMappingStatus.DRAINING -> {
                when (next) {
                    GlobalUtilizationMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationMappingStatus) {
        if (!canMoveTo(next)) {
            error("global utilization mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(discount * amount * 100) / 100

    fun snapshot(): GlobalUtilizationMapping {
        return GlobalUtilizationMapping(
            code = code,
            name = name,
            amount = amount,
            discount = discount,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global utilization mapping in status idle. */
fun newGlobalUtilizationMapping(code: String, name: String): GlobalUtilizationMapping {
    return GlobalUtilizationMapping(
        code = code,
        name = name,
        amount = 0.0,
        discount = 0.0,
        approved = false,
        status = GlobalUtilizationMappingStatus.IDLE,
    )
}
