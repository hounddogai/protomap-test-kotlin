package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient drop target is in its life cycle. */
enum class LenientDropTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient drop target. Its code is unique within a tenant. */
data class LenientDropTarget(
    var code: String,
    var title: String,
    var floorValue: Double,
    var description: String,
    var capacity: Int,
    var active: Boolean,
    var fuelLiters: Double,
    var limit: Int,
    var score: Double,
    var status: LenientDropTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropTargetStatus): Boolean {
        when (status) {
            LenientDropTargetStatus.PROPOSED -> {
                when (next) {
                    LenientDropTargetStatus.APPROVED -> return true
                }
                return false
            }
            LenientDropTargetStatus.APPROVED -> {
                when (next) {
                    LenientDropTargetStatus.REJECTED -> return true
                }
                return false
            }
            LenientDropTargetStatus.REJECTED -> return next == LenientDropTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientDropTargetStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round(floorValue * capacity.toDouble() * 100) / 100

    fun snapshot(): LenientDropTarget {
        return LenientDropTarget(
            code = code,
            title = title,
            floorValue = floorValue,
            description = description,
            capacity = capacity,
            active = active,
            fuelLiters = fuelLiters,
            limit = limit,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient drop target in status proposed. */
fun newLenientDropTarget(code: String, title: String): LenientDropTarget {
    return LenientDropTarget(
        code = code,
        title = title,
        floorValue = 0.0,
        description = "",
        capacity = 0,
        active = false,
        fuelLiters = 0.0,
        limit = 0,
        score = 0.0,
        status = LenientDropTargetStatus.PROPOSED,
    )
}
