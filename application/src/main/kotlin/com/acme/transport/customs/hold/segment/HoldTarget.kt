package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold target is in its life cycle. */
enum class HoldTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One hold target, with what the planners need to act on it. */
data class HoldTarget(
    var code: String,
    var name: String,
    var contactEmail: String,
    var reviewer: String,
    var graceDays: Int,
    var summary: String,
    var revision: Int,
    var limit: Int,
    var expiresAt: Int,
    var priority: Int,
    var capacity: Int,
    var status: HoldTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldTargetStatus): Boolean {
        when (status) {
            HoldTargetStatus.DRAFT -> return next == HoldTargetStatus.ACTIVE || next == HoldTargetStatus.SUSPENDED
            HoldTargetStatus.PENDING -> {
                when (next) {
                    HoldTargetStatus.ACTIVE, HoldTargetStatus.CLOSED -> return true
                }
                return false
            }
            HoldTargetStatus.ACTIVE -> {
                when (next) {
                    HoldTargetStatus.SUSPENDED, HoldTargetStatus.CLOSED -> return true
                }
                return false
            }
            HoldTargetStatus.SUSPENDED -> {
                when (next) {
                    HoldTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldTargetStatus) {
        if (!canMoveTo(next)) {
            error("hold target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((graceDays.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): HoldTarget {
        return HoldTarget(
            code = code,
            name = name,
            contactEmail = contactEmail,
            reviewer = reviewer,
            graceDays = graceDays,
            summary = summary,
            revision = revision,
            limit = limit,
            expiresAt = expiresAt,
            priority = priority,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold target in status draft. */
fun newHoldTarget(code: String, name: String): HoldTarget {
    return HoldTarget(
        code = code,
        name = name,
        contactEmail = "",
        reviewer = "",
        graceDays = 0,
        summary = "",
        revision = 0,
        limit = 0,
        expiresAt = 0,
        priority = 0,
        capacity = 0,
        status = HoldTargetStatus.DRAFT,
    )
}
