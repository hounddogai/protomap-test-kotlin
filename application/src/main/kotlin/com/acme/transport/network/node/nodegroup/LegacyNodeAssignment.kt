package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy node assignment is in its life cycle. */
enum class LegacyNodeAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy node assignment and its current status. */
data class LegacyNodeAssignment(
    var code: String,
    var name: String,
    var summary: String,
    var updatedAt: Int,
    var odometer: Int,
    var limit: Int,
    var archived: Boolean,
    var windowDays: Int,
    var externalRef: String,
    var weight: Double,
    var channel: String,
    var status: LegacyNodeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNodeAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNodeAssignmentStatus): Boolean {
        when (status) {
            LegacyNodeAssignmentStatus.IDLE -> {
                when (next) {
                    LegacyNodeAssignmentStatus.DRAINING, LegacyNodeAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            LegacyNodeAssignmentStatus.WARMING -> {
                when (next) {
                    LegacyNodeAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            LegacyNodeAssignmentStatus.READY -> return next == LegacyNodeAssignmentStatus.DRAINING || next == LegacyNodeAssignmentStatus.STOPPED
            LegacyNodeAssignmentStatus.DRAINING -> {
                when (next) {
                    LegacyNodeAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyNodeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy node assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((windowDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyNodeAssignment {
        return LegacyNodeAssignment(
            code = code,
            name = name,
            summary = summary,
            updatedAt = updatedAt,
            odometer = odometer,
            limit = limit,
            archived = archived,
            windowDays = windowDays,
            externalRef = externalRef,
            weight = weight,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy node assignment in status idle. */
fun newLegacyNodeAssignment(code: String, name: String): LegacyNodeAssignment {
    return LegacyNodeAssignment(
        code = code,
        name = name,
        summary = "",
        updatedAt = 0,
        odometer = 0,
        limit = 0,
        archived = false,
        windowDays = 0,
        externalRef = "",
        weight = 0.0,
        channel = "",
        status = LegacyNodeAssignmentStatus.IDLE,
    )
}
