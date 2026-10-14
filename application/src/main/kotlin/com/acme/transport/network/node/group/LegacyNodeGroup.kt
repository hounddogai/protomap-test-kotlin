package com.acme.transport.network.node.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy node group records of network's node work, and the rules, stores, and calculations around them.

/** Where a legacy node group is in its life cycle. */
enum class LegacyNodeGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy node group and its current status. */
data class LegacyNodeGroup(
    var code: String,
    var title: String,
    var routeCode: String,
    var ceiling: Double,
    var driverId: String,
    var notes: String,
    var amount: Double,
    var status: LegacyNodeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNodeGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNodeGroupStatus): Boolean {
        when (status) {
            LegacyNodeGroupStatus.PLANNED -> {
                when (next) {
                    LegacyNodeGroupStatus.SCHEDULED -> return true
                }
                return false
            }
            LegacyNodeGroupStatus.SCHEDULED -> {
                when (next) {
                    LegacyNodeGroupStatus.COMPLETED, LegacyNodeGroupStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyNodeGroupStatus.IN_PROGRESS -> return next == LegacyNodeGroupStatus.COMPLETED || next == LegacyNodeGroupStatus.CANCELLED
            LegacyNodeGroupStatus.COMPLETED -> return next == LegacyNodeGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyNodeGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy node group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((amount + ceiling) * 100) / 100

    fun snapshot(): LegacyNodeGroup {
        return LegacyNodeGroup(
            code = code,
            title = title,
            routeCode = routeCode,
            ceiling = ceiling,
            driverId = driverId,
            notes = notes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy node group in status planned. */
fun newLegacyNodeGroup(code: String, title: String): LegacyNodeGroup {
    return LegacyNodeGroup(
        code = code,
        title = title,
        routeCode = "",
        ceiling = 0.0,
        driverId = "",
        notes = "",
        amount = 0.0,
        status = LegacyNodeGroupStatus.PLANNED,
    )
}
