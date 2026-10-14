package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary near miss segment is in its life cycle. */
enum class PrimaryNearMissSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary near miss segment as the near miss team keeps it. */
data class PrimaryNearMissSegment(
    var code: String,
    var title: String,
    var cost: Double,
    var unit: String,
    var approved: Boolean,
    var pallets: Int,
    var startsAt: Int,
    var attributes: MutableMap<String, String>,
    var status: PrimaryNearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNearMissSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNearMissSegmentStatus): Boolean {
        when (status) {
            PrimaryNearMissSegmentStatus.PLANNED -> {
                when (next) {
                    PrimaryNearMissSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            PrimaryNearMissSegmentStatus.SCHEDULED -> {
                when (next) {
                    PrimaryNearMissSegmentStatus.IN_PROGRESS, PrimaryNearMissSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryNearMissSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryNearMissSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryNearMissSegmentStatus.COMPLETED -> return next == PrimaryNearMissSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryNearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary near miss segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((startsAt.toDouble() - cost) * 100) / 100

    fun snapshot(): PrimaryNearMissSegment {
        return PrimaryNearMissSegment(
            code = code,
            title = title,
            cost = cost,
            unit = unit,
            approved = approved,
            pallets = pallets,
            startsAt = startsAt,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary near miss segment in status planned. */
fun newPrimaryNearMissSegment(code: String, title: String): PrimaryNearMissSegment {
    return PrimaryNearMissSegment(
        code = code,
        title = title,
        cost = 0.0,
        unit = "",
        approved = false,
        pallets = 0,
        startsAt = 0,
        attributes = mutableMapOf<String, String>(),
        status = PrimaryNearMissSegmentStatus.PLANNED,
    )
}
