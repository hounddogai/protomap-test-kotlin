package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy utilization segment is in its life cycle. */
enum class LegacyUtilizationSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy utilization segment and its current status. */
data class LegacyUtilizationSegment(
    var code: String,
    var name: String,
    var startsAt: Int,
    var active: Boolean,
    var discount: Double,
    var quantity: Int,
    var description: String,
    var limit: Int,
    var label: String,
    var status: LegacyUtilizationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationSegmentStatus): Boolean {
        when (status) {
            LegacyUtilizationSegmentStatus.DRAFT -> return next == LegacyUtilizationSegmentStatus.PENDING
            LegacyUtilizationSegmentStatus.PENDING -> {
                when (next) {
                    LegacyUtilizationSegmentStatus.CLOSED -> return true
                }
                return false
            }
            LegacyUtilizationSegmentStatus.ACTIVE -> return next == LegacyUtilizationSegmentStatus.CLOSED
            LegacyUtilizationSegmentStatus.SUSPENDED -> {
                when (next) {
                    LegacyUtilizationSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy utilization segment"} $code"

    fun effectiveValue(): Double = round(discount * quantity.toDouble() * 100) / 100

    fun snapshot(): LegacyUtilizationSegment {
        return LegacyUtilizationSegment(
            code = code,
            name = name,
            startsAt = startsAt,
            active = active,
            discount = discount,
            quantity = quantity,
            description = description,
            limit = limit,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy utilization segment in status draft. */
fun newLegacyUtilizationSegment(code: String, name: String): LegacyUtilizationSegment {
    return LegacyUtilizationSegment(
        code = code,
        name = name,
        startsAt = 0,
        active = false,
        discount = 0.0,
        quantity = 0,
        description = "",
        limit = 0,
        label = "",
        status = LegacyUtilizationSegmentStatus.DRAFT,
    )
}
