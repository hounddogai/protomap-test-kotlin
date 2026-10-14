package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy clearance target is in its life cycle. */
enum class LegacyClearanceTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy clearance target as the clearance team keeps it. */
data class LegacyClearanceTarget(
    var code: String,
    var label: String,
    var locale: String,
    var ratio: Double,
    var name: String,
    var priority: Int,
    var pallets: Int,
    var status: LegacyClearanceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceTargetStatus): Boolean {
        when (status) {
            LegacyClearanceTargetStatus.NEW -> {
                when (next) {
                    LegacyClearanceTargetStatus.DONE -> return true
                }
                return false
            }
            LegacyClearanceTargetStatus.QUEUED -> return next == LegacyClearanceTargetStatus.DONE
            LegacyClearanceTargetStatus.RUNNING -> {
                when (next) {
                    LegacyClearanceTargetStatus.DONE -> return true
                }
                return false
            }
            LegacyClearanceTargetStatus.DONE -> return next == LegacyClearanceTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyClearanceTargetStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy clearance target"} $code"

    fun projectedLoad(): Double = round((pallets.toDouble() + ratio) * 100) / 100

    fun snapshot(): LegacyClearanceTarget {
        return LegacyClearanceTarget(
            code = code,
            label = label,
            locale = locale,
            ratio = ratio,
            name = name,
            priority = priority,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy clearance target in status new. */
fun newLegacyClearanceTarget(code: String, label: String): LegacyClearanceTarget {
    return LegacyClearanceTarget(
        code = code,
        label = label,
        locale = "",
        ratio = 0.0,
        name = "",
        priority = 0,
        pallets = 0,
        status = LegacyClearanceTargetStatus.NEW,
    )
}
