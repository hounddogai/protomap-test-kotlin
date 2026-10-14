package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop allocation is in its life cycle. */
enum class DropAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A drop allocation as the drop team keeps it. */
data class DropAllocation(
    var code: String,
    var name: String,
    var segment: String,
    var retries: Int,
    var etaMinutes: Int,
    var shiftCode: String,
    var title: String,
    var fuelLiters: Double,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var status: DropAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropAllocationStatus): Boolean {
        when (status) {
            DropAllocationStatus.OPEN -> {
                when (next) {
                    DropAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            DropAllocationStatus.IN_REVIEW -> return next == DropAllocationStatus.REOPENED
            DropAllocationStatus.RESOLVED -> {
                when (next) {
                    DropAllocationStatus.REOPENED -> return true
                }
                return false
            }
            DropAllocationStatus.REOPENED -> return next == DropAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropAllocationStatus) {
        if (!canMoveTo(next)) {
            error("drop allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((retries.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): DropAllocation {
        return DropAllocation(
            code = code,
            name = name,
            segment = segment,
            retries = retries,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            title = title,
            fuelLiters = fuelLiters,
            channel = channel,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a drop allocation in status open. */
fun newDropAllocation(code: String, name: String): DropAllocation {
    return DropAllocation(
        code = code,
        name = name,
        segment = "",
        retries = 0,
        etaMinutes = 0,
        shiftCode = "",
        title = "",
        fuelLiters = 0.0,
        channel = "",
        attributes = mutableMapOf<String, String>(),
        status = DropAllocationStatus.OPEN,
    )
}
