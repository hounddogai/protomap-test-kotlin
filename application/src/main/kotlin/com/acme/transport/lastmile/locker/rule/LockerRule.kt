package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker rule records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker rule is in its life cycle. */
enum class LockerRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A locker rule as the locker team keeps it. */
data class LockerRule(
    var code: String,
    var title: String,
    var version: Int,
    var reviewer: String,
    var vehicleId: String,
    var weight: Double,
    var unit: String,
    var startsAt: Int,
    var dock: String,
    var pinned: Boolean,
    var sequence: Int,
    var status: LockerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerRuleStatus): Boolean {
        when (status) {
            LockerRuleStatus.PLANNED -> {
                when (next) {
                    LockerRuleStatus.SCHEDULED, LockerRuleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LockerRuleStatus.SCHEDULED -> {
                when (next) {
                    LockerRuleStatus.IN_PROGRESS, LockerRuleStatus.CANCELLED -> return true
                }
                return false
            }
            LockerRuleStatus.IN_PROGRESS -> return next == LockerRuleStatus.COMPLETED || next == LockerRuleStatus.CANCELLED
            LockerRuleStatus.COMPLETED -> {
                when (next) {
                    LockerRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerRuleStatus) {
        if (!canMoveTo(next)) {
            error("locker rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"locker rule"} $code"

    fun effectiveExposure(): Double = round((startsAt.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): LockerRule {
        return LockerRule(
            code = code,
            title = title,
            version = version,
            reviewer = reviewer,
            vehicleId = vehicleId,
            weight = weight,
            unit = unit,
            startsAt = startsAt,
            dock = dock,
            pinned = pinned,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a locker rule in status planned. */
fun newLockerRule(code: String, title: String): LockerRule {
    return LockerRule(
        code = code,
        title = title,
        version = 0,
        reviewer = "",
        vehicleId = "",
        weight = 0.0,
        unit = "",
        startsAt = 0,
        dock = "",
        pinned = false,
        sequence = 0,
        status = LockerRuleStatus.PLANNED,
    )
}
