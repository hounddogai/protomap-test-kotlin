package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional rest break adjustment is in its life cycle. */
enum class RegionalRestBreakAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional rest break adjustment, with what the planners need to act on it. */
data class RegionalRestBreakAdjustment(
    var code: String,
    var name: String,
    var description: String,
    var vehicleId: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var floorValue: Double,
    var tags: MutableList<String>,
    var contactEmail: String,
    var stopCount: Int,
    var status: RegionalRestBreakAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRestBreakAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRestBreakAdjustmentStatus): Boolean {
        when (status) {
            RegionalRestBreakAdjustmentStatus.CREATED -> {
                when (next) {
                    RegionalRestBreakAdjustmentStatus.PUBLISHED, RegionalRestBreakAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            RegionalRestBreakAdjustmentStatus.VALIDATED -> return next == RegionalRestBreakAdjustmentStatus.PUBLISHED || next == RegionalRestBreakAdjustmentStatus.RETIRED
            RegionalRestBreakAdjustmentStatus.PUBLISHED -> return next == RegionalRestBreakAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalRestBreakAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional rest break adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional rest break adjustment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((etaMinutes.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RegionalRestBreakAdjustment {
        return RegionalRestBreakAdjustment(
            code = code,
            name = name,
            description = description,
            vehicleId = vehicleId,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            floorValue = floorValue,
            tags = tags,
            contactEmail = contactEmail,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional rest break adjustment in status created. */
fun newRegionalRestBreakAdjustment(code: String, name: String): RegionalRestBreakAdjustment {
    return RegionalRestBreakAdjustment(
        code = code,
        name = name,
        description = "",
        vehicleId = "",
        etaMinutes = 0,
        expiresAt = 0,
        floorValue = 0.0,
        tags = mutableListOf<String>(),
        contactEmail = "",
        stopCount = 0,
        status = RegionalRestBreakAdjustmentStatus.CREATED,
    )
}
