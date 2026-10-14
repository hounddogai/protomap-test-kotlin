package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling drop milestone is in its life cycle. */
enum class RollingDropMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling drop milestone and its current status. */
data class RollingDropMilestone(
    var code: String,
    var label: String,
    var ratio: Double,
    var ceiling: Double,
    var name: String,
    var locale: String,
    var title: String,
    var version: Int,
    var shiftCode: String,
    var minimum: Double,
    var status: RollingDropMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDropMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDropMilestoneStatus): Boolean {
        when (status) {
            RollingDropMilestoneStatus.REQUESTED -> return next == RollingDropMilestoneStatus.EXPIRED
            RollingDropMilestoneStatus.GRANTED -> return next == RollingDropMilestoneStatus.REVOKED || next == RollingDropMilestoneStatus.EXPIRED
            RollingDropMilestoneStatus.REVOKED -> return next == RollingDropMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingDropMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling drop milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((version.toDouble() - ratio) * 100) / 100

    fun snapshot(): RollingDropMilestone {
        return RollingDropMilestone(
            code = code,
            label = label,
            ratio = ratio,
            ceiling = ceiling,
            name = name,
            locale = locale,
            title = title,
            version = version,
            shiftCode = shiftCode,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling drop milestone in status requested. */
fun newRollingDropMilestone(code: String, label: String): RollingDropMilestone {
    return RollingDropMilestone(
        code = code,
        label = label,
        ratio = 0.0,
        ceiling = 0.0,
        name = "",
        locale = "",
        title = "",
        version = 0,
        shiftCode = "",
        minimum = 0.0,
        status = RollingDropMilestoneStatus.REQUESTED,
    )
}
