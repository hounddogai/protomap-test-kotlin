package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling idling segment is in its life cycle. */
enum class RollingIdlingSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling idling segment. Its code is unique within a tenant. */
data class RollingIdlingSegment(
    var code: String,
    var name: String,
    var unit: String,
    var floorValue: Double,
    var contactEmail: String,
    var priority: Int,
    var ratio: Double,
    var maximum: Double,
    var version: Int,
    var cost: Double,
    var status: RollingIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIdlingSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIdlingSegmentStatus): Boolean {
        when (status) {
            RollingIdlingSegmentStatus.DRAFT -> return next == RollingIdlingSegmentStatus.ACTIVE || next == RollingIdlingSegmentStatus.CLOSED
            RollingIdlingSegmentStatus.PENDING -> return next == RollingIdlingSegmentStatus.ACTIVE
            RollingIdlingSegmentStatus.ACTIVE -> {
                when (next) {
                    RollingIdlingSegmentStatus.CLOSED -> return true
                }
                return false
            }
            RollingIdlingSegmentStatus.SUSPENDED -> {
                when (next) {
                    RollingIdlingSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling idling segment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((cost - floorValue) * 100) / 100

    fun snapshot(): RollingIdlingSegment {
        return RollingIdlingSegment(
            code = code,
            name = name,
            unit = unit,
            floorValue = floorValue,
            contactEmail = contactEmail,
            priority = priority,
            ratio = ratio,
            maximum = maximum,
            version = version,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling idling segment in status draft. */
fun newRollingIdlingSegment(code: String, name: String): RollingIdlingSegment {
    return RollingIdlingSegment(
        code = code,
        name = name,
        unit = "",
        floorValue = 0.0,
        contactEmail = "",
        priority = 0,
        ratio = 0.0,
        maximum = 0.0,
        version = 0,
        cost = 0.0,
        status = RollingIdlingSegmentStatus.DRAFT,
    )
}
