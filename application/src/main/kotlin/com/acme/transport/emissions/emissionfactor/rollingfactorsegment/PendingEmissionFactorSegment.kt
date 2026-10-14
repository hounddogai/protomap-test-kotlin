package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.normalize

// The pending emission factor segment records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a pending emission factor segment is in its life cycle. */
enum class PendingEmissionFactorSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending emission factor segment and its current status. */
data class PendingEmissionFactorSegment(
    var code: String,
    var title: String,
    var archived: Boolean,
    var ceiling: Double,
    var approved: Boolean,
    var status: PendingEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEmissionFactorSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEmissionFactorSegmentStatus): Boolean {
        when (status) {
            PendingEmissionFactorSegmentStatus.PLANNED -> return next == PendingEmissionFactorSegmentStatus.SCHEDULED
            PendingEmissionFactorSegmentStatus.SCHEDULED -> {
                when (next) {
                    PendingEmissionFactorSegmentStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingEmissionFactorSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    PendingEmissionFactorSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            PendingEmissionFactorSegmentStatus.COMPLETED -> return next == PendingEmissionFactorSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending emission factor segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = ceiling

    fun snapshot(): PendingEmissionFactorSegment {
        return PendingEmissionFactorSegment(
            code = code,
            title = title,
            archived = archived,
            ceiling = ceiling,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending emission factor segment in status planned. */
fun newPendingEmissionFactorSegment(code: String, title: String): PendingEmissionFactorSegment {
    return PendingEmissionFactorSegment(
        code = code,
        title = title,
        archived = false,
        ceiling = 0.0,
        approved = false,
        status = PendingEmissionFactorSegmentStatus.PLANNED,
    )
}
