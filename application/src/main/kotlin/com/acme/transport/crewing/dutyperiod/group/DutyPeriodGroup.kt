package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period group records of crewing's duty period work, and the rules, stores, and calculations around
// them.

/** Where a duty period group is in its life cycle. */
enum class DutyPeriodGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A duty period group as the duty period team keeps it. */
data class DutyPeriodGroup(
    var code: String,
    var title: String,
    var limit: Int,
    var loadKg: Double,
    var pallets: Int,
    var routeCode: String,
    var rate: Double,
    var trailerId: String,
    var expiresAt: Int,
    var windowDays: Int,
    var archived: Boolean,
    var status: DutyPeriodGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodGroupStatus): Boolean {
        when (status) {
            DutyPeriodGroupStatus.IDLE -> {
                when (next) {
                    DutyPeriodGroupStatus.WARMING -> return true
                }
                return false
            }
            DutyPeriodGroupStatus.WARMING -> return next == DutyPeriodGroupStatus.DRAINING || next == DutyPeriodGroupStatus.STOPPED
            DutyPeriodGroupStatus.READY -> return next == DutyPeriodGroupStatus.DRAINING
            DutyPeriodGroupStatus.DRAINING -> return next == DutyPeriodGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DutyPeriodGroupStatus) {
        if (!canMoveTo(next)) {
            error("duty period group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"duty period group"} $code"

    fun adjustedLoad(): Double = round((rate + pallets.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodGroup {
        return DutyPeriodGroup(
            code = code,
            title = title,
            limit = limit,
            loadKg = loadKg,
            pallets = pallets,
            routeCode = routeCode,
            rate = rate,
            trailerId = trailerId,
            expiresAt = expiresAt,
            windowDays = windowDays,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period group in status idle. */
fun newDutyPeriodGroup(code: String, title: String): DutyPeriodGroup {
    return DutyPeriodGroup(
        code = code,
        title = title,
        limit = 0,
        loadKg = 0.0,
        pallets = 0,
        routeCode = "",
        rate = 0.0,
        trailerId = "",
        expiresAt = 0,
        windowDays = 0,
        archived = false,
        status = DutyPeriodGroupStatus.IDLE,
    )
}
