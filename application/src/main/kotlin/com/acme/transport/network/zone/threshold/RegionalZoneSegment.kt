package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional zone segment is in its life cycle. */
enum class RegionalZoneSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional zone segment. Its code is unique within a tenant. */
data class RegionalZoneSegment(
    var code: String,
    var title: String,
    var unit: String,
    var createdAt: Int,
    var attempts: Int,
    var quantity: Int,
    var label: String,
    var minimum: Double,
    var currency: String,
    var status: RegionalZoneSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneSegmentStatus): Boolean {
        when (status) {
            RegionalZoneSegmentStatus.OPEN -> {
                when (next) {
                    RegionalZoneSegmentStatus.RESOLVED -> return true
                }
                return false
            }
            RegionalZoneSegmentStatus.IN_REVIEW -> return next == RegionalZoneSegmentStatus.ARCHIVED
            RegionalZoneSegmentStatus.RESOLVED -> {
                when (next) {
                    RegionalZoneSegmentStatus.REOPENED, RegionalZoneSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalZoneSegmentStatus.REOPENED -> return next == RegionalZoneSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalZoneSegmentStatus) {
        if (!canMoveTo(next)) {
            error("regional zone segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedValue(): Double = round(createdAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RegionalZoneSegment {
        return RegionalZoneSegment(
            code = code,
            title = title,
            unit = unit,
            createdAt = createdAt,
            attempts = attempts,
            quantity = quantity,
            label = label,
            minimum = minimum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional zone segment in status open. */
fun newRegionalZoneSegment(code: String, title: String): RegionalZoneSegment {
    return RegionalZoneSegment(
        code = code,
        title = title,
        unit = "",
        createdAt = 0,
        attempts = 0,
        quantity = 0,
        label = "",
        minimum = 0.0,
        currency = "",
        status = RegionalZoneSegmentStatus.OPEN,
    )
}
