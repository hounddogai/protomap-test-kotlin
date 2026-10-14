package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize

/** Where a fallback parcel target is in its life cycle. */
enum class FallbackParcelTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback parcel target. Its code is unique within a tenant. */
data class FallbackParcelTarget(
    var code: String,
    var title: String,
    var summary: String,
    var ownerId: String,
    var sequence: Int,
    var status: FallbackParcelTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackParcelTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackParcelTargetStatus): Boolean {
        when (status) {
            FallbackParcelTargetStatus.CREATED -> {
                when (next) {
                    FallbackParcelTargetStatus.PUBLISHED, FallbackParcelTargetStatus.RETIRED -> return true
                }
                return false
            }
            FallbackParcelTargetStatus.VALIDATED -> return next == FallbackParcelTargetStatus.PUBLISHED || next == FallbackParcelTargetStatus.RETIRED
            FallbackParcelTargetStatus.PUBLISHED -> return next == FallbackParcelTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackParcelTargetStatus) {
        if (!canMoveTo(next)) {
            error("fallback parcel target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): FallbackParcelTarget {
        return FallbackParcelTarget(
            code = code,
            title = title,
            summary = summary,
            ownerId = ownerId,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback parcel target in status created. */
fun newFallbackParcelTarget(code: String, title: String): FallbackParcelTarget {
    return FallbackParcelTarget(
        code = code,
        title = title,
        summary = "",
        ownerId = "",
        sequence = 0,
        status = FallbackParcelTargetStatus.CREATED,
    )
}
