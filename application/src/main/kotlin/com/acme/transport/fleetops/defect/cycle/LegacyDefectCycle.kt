package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy defect cycle records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a legacy defect cycle is in its life cycle. */
enum class LegacyDefectCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy defect cycle and its current status. */
data class LegacyDefectCycle(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var driverId: String,
    var approved: Boolean,
    var tags: MutableList<String>,
    var phone: String,
    var minimum: Double,
    var region: String,
    var status: LegacyDefectCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDefectCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDefectCycleStatus): Boolean {
        when (status) {
            LegacyDefectCycleStatus.REQUESTED -> return next == LegacyDefectCycleStatus.GRANTED || next == LegacyDefectCycleStatus.EXPIRED
            LegacyDefectCycleStatus.GRANTED -> {
                when (next) {
                    LegacyDefectCycleStatus.REVOKED, LegacyDefectCycleStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyDefectCycleStatus.REVOKED -> {
                when (next) {
                    LegacyDefectCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDefectCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy defect cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((durationMinutes.toDouble() - minimum) * 100) / 100

    fun snapshot(): LegacyDefectCycle {
        return LegacyDefectCycle(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            driverId = driverId,
            approved = approved,
            tags = tags,
            phone = phone,
            minimum = minimum,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy defect cycle in status requested. */
fun newLegacyDefectCycle(code: String, title: String): LegacyDefectCycle {
    return LegacyDefectCycle(
        code = code,
        title = title,
        durationMinutes = 0,
        driverId = "",
        approved = false,
        tags = mutableListOf<String>(),
        phone = "",
        minimum = 0.0,
        region = "",
        status = LegacyDefectCycleStatus.REQUESTED,
    )
}
