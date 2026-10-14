package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone plan is in its life cycle. */
enum class ZonePlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a zone plan and its current status. */
data class ZonePlan(
    var code: String,
    var title: String,
    var locale: String,
    var budget: Double,
    var approved: Boolean,
    var lane: String,
    var active: Boolean,
    var ratio: Double,
    var status: ZonePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZonePlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZonePlanStatus): Boolean {
        when (status) {
            ZonePlanStatus.PROPOSED -> return next == ZonePlanStatus.REJECTED || next == ZonePlanStatus.WITHDRAWN
            ZonePlanStatus.APPROVED -> return next == ZonePlanStatus.REJECTED || next == ZonePlanStatus.WITHDRAWN
            ZonePlanStatus.REJECTED -> {
                when (next) {
                    ZonePlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZonePlanStatus) {
        if (!canMoveTo(next)) {
            error("zone plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((ratio + budget) * 100) / 100

    fun snapshot(): ZonePlan {
        return ZonePlan(
            code = code,
            title = title,
            locale = locale,
            budget = budget,
            approved = approved,
            lane = lane,
            active = active,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone plan in status proposed. */
fun newZonePlan(code: String, title: String): ZonePlan {
    return ZonePlan(
        code = code,
        title = title,
        locale = "",
        budget = 0.0,
        approved = false,
        lane = "",
        active = false,
        ratio = 0.0,
        status = ZonePlanStatus.PROPOSED,
    )
}
