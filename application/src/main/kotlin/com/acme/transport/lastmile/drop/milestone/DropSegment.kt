package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop segment is in its life cycle. */
enum class DropSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One drop segment, with what the planners need to act on it. */
data class DropSegment(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var amount: Double,
    var externalRef: String,
    var notes: String,
    var routeCode: String,
    var label: String,
    var status: DropSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropSegmentStatus): Boolean {
        when (status) {
            DropSegmentStatus.OPEN -> return next == DropSegmentStatus.IN_REVIEW || next == DropSegmentStatus.ARCHIVED
            DropSegmentStatus.IN_REVIEW -> return next == DropSegmentStatus.REOPENED || next == DropSegmentStatus.ARCHIVED
            DropSegmentStatus.RESOLVED -> return next == DropSegmentStatus.REOPENED
            DropSegmentStatus.REOPENED -> return next == DropSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DropSegmentStatus) {
        if (!canMoveTo(next)) {
            error("drop segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop segment"} $code"

    fun weightedLoad(): Double = round((amount - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DropSegment {
        return DropSegment(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            amount = amount,
            externalRef = externalRef,
            notes = notes,
            routeCode = routeCode,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a drop segment in status open. */
fun newDropSegment(code: String, name: String): DropSegment {
    return DropSegment(
        code = code,
        name = name,
        etaMinutes = 0,
        amount = 0.0,
        externalRef = "",
        notes = "",
        routeCode = "",
        label = "",
        status = DropSegmentStatus.OPEN,
    )
}
