package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cross dock assignment is in its life cycle. */
enum class LegacyCrossDockAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy cross dock assignment as the cross dock team keeps it. */
data class LegacyCrossDockAssignment(
    var code: String,
    var label: String,
    var shiftCode: String,
    var endsAt: Int,
    var archived: Boolean,
    var description: String,
    var threshold: Double,
    var name: String,
    var status: LegacyCrossDockAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockAssignmentStatus): Boolean {
        when (status) {
            LegacyCrossDockAssignmentStatus.IDLE -> return next == LegacyCrossDockAssignmentStatus.WARMING || next == LegacyCrossDockAssignmentStatus.READY
            LegacyCrossDockAssignmentStatus.WARMING -> return next == LegacyCrossDockAssignmentStatus.READY || next == LegacyCrossDockAssignmentStatus.STOPPED
            LegacyCrossDockAssignmentStatus.READY -> {
                when (next) {
                    LegacyCrossDockAssignmentStatus.DRAINING, LegacyCrossDockAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            LegacyCrossDockAssignmentStatus.DRAINING -> return next == LegacyCrossDockAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy cross dock assignment"} $code"

    fun weightedValue(): Double = round(endsAt.toDouble() * threshold * 100) / 100

    fun snapshot(): LegacyCrossDockAssignment {
        return LegacyCrossDockAssignment(
            code = code,
            label = label,
            shiftCode = shiftCode,
            endsAt = endsAt,
            archived = archived,
            description = description,
            threshold = threshold,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy cross dock assignment in status idle. */
fun newLegacyCrossDockAssignment(code: String, label: String): LegacyCrossDockAssignment {
    return LegacyCrossDockAssignment(
        code = code,
        label = label,
        shiftCode = "",
        endsAt = 0,
        archived = false,
        description = "",
        threshold = 0.0,
        name = "",
        status = LegacyCrossDockAssignmentStatus.IDLE,
    )
}
