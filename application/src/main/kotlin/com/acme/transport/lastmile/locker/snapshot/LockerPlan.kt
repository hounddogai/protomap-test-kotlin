package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker plan is in its life cycle. */
enum class LockerPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a locker plan and its current status. */
data class LockerPlan(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var retries: Int,
    var locale: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var status: LockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerPlanStatus): Boolean {
        when (status) {
            LockerPlanStatus.OPEN -> return next == LockerPlanStatus.IN_REVIEW || next == LockerPlanStatus.RESOLVED
            LockerPlanStatus.IN_REVIEW -> {
                when (next) {
                    LockerPlanStatus.RESOLVED, LockerPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            LockerPlanStatus.RESOLVED -> return next == LockerPlanStatus.ARCHIVED
            LockerPlanStatus.REOPENED -> return next == LockerPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("locker plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((updatedAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LockerPlan {
        return LockerPlan(
            code = code,
            title = title,
            updatedAt = updatedAt,
            retries = retries,
            locale = locale,
            channel = channel,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a locker plan in status open. */
fun newLockerPlan(code: String, title: String): LockerPlan {
    return LockerPlan(
        code = code,
        title = title,
        updatedAt = 0,
        retries = 0,
        locale = "",
        channel = "",
        attributes = mutableMapOf<String, String>(),
        status = LockerPlanStatus.OPEN,
    )
}
