package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker target is in its life cycle. */
enum class LockerTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A locker target as the locker team keeps it. */
data class LockerTarget(
    var code: String,
    var name: String,
    var reviewer: String,
    var phone: String,
    var ceiling: Double,
    var maximum: Double,
    var margin: Double,
    var updatedAt: Int,
    var category: String,
    var amount: Double,
    var status: LockerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerTargetStatus): Boolean {
        when (status) {
            LockerTargetStatus.PLANNED -> {
                when (next) {
                    LockerTargetStatus.COMPLETED, LockerTargetStatus.CANCELLED -> return true
                }
                return false
            }
            LockerTargetStatus.SCHEDULED -> {
                when (next) {
                    LockerTargetStatus.IN_PROGRESS, LockerTargetStatus.COMPLETED -> return true
                }
                return false
            }
            LockerTargetStatus.IN_PROGRESS -> {
                when (next) {
                    LockerTargetStatus.CANCELLED -> return true
                }
                return false
            }
            LockerTargetStatus.COMPLETED -> return next == LockerTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LockerTargetStatus) {
        if (!canMoveTo(next)) {
            error("locker target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round((updatedAt.toDouble() + maximum) * 100) / 100

    fun snapshot(): LockerTarget {
        return LockerTarget(
            code = code,
            name = name,
            reviewer = reviewer,
            phone = phone,
            ceiling = ceiling,
            maximum = maximum,
            margin = margin,
            updatedAt = updatedAt,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a locker target in status planned. */
fun newLockerTarget(code: String, name: String): LockerTarget {
    return LockerTarget(
        code = code,
        name = name,
        reviewer = "",
        phone = "",
        ceiling = 0.0,
        maximum = 0.0,
        margin = 0.0,
        updatedAt = 0,
        category = "",
        amount = 0.0,
        status = LockerTargetStatus.PLANNED,
    )
}
