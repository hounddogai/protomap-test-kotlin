package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global idling segment is in its life cycle. */
enum class GlobalIdlingSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global idling segment as the idling team keeps it. */
data class GlobalIdlingSegment(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var segment: String,
    var reviewer: String,
    var weight: Double,
    var budget: Double,
    var status: GlobalIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIdlingSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIdlingSegmentStatus): Boolean {
        when (status) {
            GlobalIdlingSegmentStatus.DRAFT -> return next == GlobalIdlingSegmentStatus.PENDING || next == GlobalIdlingSegmentStatus.SUSPENDED
            GlobalIdlingSegmentStatus.PENDING -> return next == GlobalIdlingSegmentStatus.ACTIVE || next == GlobalIdlingSegmentStatus.SUSPENDED
            GlobalIdlingSegmentStatus.ACTIVE -> return next == GlobalIdlingSegmentStatus.SUSPENDED || next == GlobalIdlingSegmentStatus.CLOSED
            GlobalIdlingSegmentStatus.SUSPENDED -> {
                when (next) {
                    GlobalIdlingSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global idling segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global idling segment"} $code"

    fun projectedTotal(): Double = round((budget - weight) * 100) / 100

    fun snapshot(): GlobalIdlingSegment {
        return GlobalIdlingSegment(
            code = code,
            name = name,
            updatedAt = updatedAt,
            segment = segment,
            reviewer = reviewer,
            weight = weight,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global idling segment in status draft. */
fun newGlobalIdlingSegment(code: String, name: String): GlobalIdlingSegment {
    return GlobalIdlingSegment(
        code = code,
        name = name,
        updatedAt = 0,
        segment = "",
        reviewer = "",
        weight = 0.0,
        budget = 0.0,
        status = GlobalIdlingSegmentStatus.DRAFT,
    )
}
