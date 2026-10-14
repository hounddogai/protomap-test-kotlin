package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff assignment is in its life cycle. */
enum class CutoffAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A cutoff assignment. Its code is unique within a tenant. */
data class CutoffAssignment(
    var code: String,
    var title: String,
    var lane: String,
    var quantity: Int,
    var floorValue: Double,
    var channel: String,
    var segment: String,
    var attempts: Int,
    var status: CutoffAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffAssignmentStatus): Boolean {
        when (status) {
            CutoffAssignmentStatus.REQUESTED -> return next == CutoffAssignmentStatus.EXPIRED
            CutoffAssignmentStatus.GRANTED -> {
                when (next) {
                    CutoffAssignmentStatus.REVOKED, CutoffAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            CutoffAssignmentStatus.REVOKED -> return next == CutoffAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CutoffAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("cutoff assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round((floorValue + quantity.toDouble()) * 100) / 100

    fun snapshot(): CutoffAssignment {
        return CutoffAssignment(
            code = code,
            title = title,
            lane = lane,
            quantity = quantity,
            floorValue = floorValue,
            channel = channel,
            segment = segment,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a cutoff assignment in status requested. */
fun newCutoffAssignment(code: String, title: String): CutoffAssignment {
    return CutoffAssignment(
        code = code,
        title = title,
        lane = "",
        quantity = 0,
        floorValue = 0.0,
        channel = "",
        segment = "",
        attempts = 0,
        status = CutoffAssignmentStatus.REQUESTED,
    )
}
