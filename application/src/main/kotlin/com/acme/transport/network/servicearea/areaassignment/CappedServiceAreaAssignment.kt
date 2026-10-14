package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped service area assignment records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a capped service area assignment is in its life cycle. */
enum class CappedServiceAreaAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped service area assignment. Its code is unique within a tenant. */
data class CappedServiceAreaAssignment(
    var code: String,
    var name: String,
    var stopCount: Int,
    var limit: Int,
    var startsAt: Int,
    var sequence: Int,
    var status: CappedServiceAreaAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServiceAreaAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServiceAreaAssignmentStatus): Boolean {
        when (status) {
            CappedServiceAreaAssignmentStatus.DRAFT -> {
                when (next) {
                    CappedServiceAreaAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedServiceAreaAssignmentStatus.PENDING -> return next == CappedServiceAreaAssignmentStatus.ACTIVE || next == CappedServiceAreaAssignmentStatus.CLOSED
            CappedServiceAreaAssignmentStatus.ACTIVE -> {
                when (next) {
                    CappedServiceAreaAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedServiceAreaAssignmentStatus.SUSPENDED -> return next == CappedServiceAreaAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedServiceAreaAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped service area assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped service area assignment"} $code"

    fun weightedValue(): Double = round(sequence.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): CappedServiceAreaAssignment {
        return CappedServiceAreaAssignment(
            code = code,
            name = name,
            stopCount = stopCount,
            limit = limit,
            startsAt = startsAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped service area assignment in status draft. */
fun newCappedServiceAreaAssignment(code: String, name: String): CappedServiceAreaAssignment {
    return CappedServiceAreaAssignment(
        code = code,
        name = name,
        stopCount = 0,
        limit = 0,
        startsAt = 0,
        sequence = 0,
        status = CappedServiceAreaAssignmentStatus.DRAFT,
    )
}
