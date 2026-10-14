package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient clearance checkpoint is in its life cycle. */
enum class LenientClearanceCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient clearance checkpoint and its current status. */
data class LenientClearanceCheckpoint(
    var code: String,
    var name: String,
    var maximum: Double,
    var unit: String,
    var dock: String,
    var summary: String,
    var margin: Double,
    var status: LenientClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientClearanceCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientClearanceCheckpointStatus): Boolean {
        when (status) {
            LenientClearanceCheckpointStatus.REQUESTED -> return next == LenientClearanceCheckpointStatus.REVOKED || next == LenientClearanceCheckpointStatus.EXPIRED
            LenientClearanceCheckpointStatus.GRANTED -> return next == LenientClearanceCheckpointStatus.EXPIRED
            LenientClearanceCheckpointStatus.REVOKED -> return next == LenientClearanceCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((margin - maximum) * 100) / 100

    fun snapshot(): LenientClearanceCheckpoint {
        return LenientClearanceCheckpoint(
            code = code,
            name = name,
            maximum = maximum,
            unit = unit,
            dock = dock,
            summary = summary,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient clearance checkpoint in status requested. */
fun newLenientClearanceCheckpoint(code: String, name: String): LenientClearanceCheckpoint {
    return LenientClearanceCheckpoint(
        code = code,
        name = name,
        maximum = 0.0,
        unit = "",
        dock = "",
        summary = "",
        margin = 0.0,
        status = LenientClearanceCheckpointStatus.REQUESTED,
    )
}
