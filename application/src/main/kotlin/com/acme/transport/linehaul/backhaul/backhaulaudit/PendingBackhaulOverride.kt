package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending backhaul override is in its life cycle. */
enum class PendingBackhaulOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending backhaul override and its current status. */
data class PendingBackhaulOverride(
    var code: String,
    var name: String,
    var ratio: Double,
    var durationMinutes: Int,
    var category: String,
    var startsAt: Int,
    var depot: String,
    var description: String,
    var reviewer: String,
    var visible: Boolean,
    var status: PendingBackhaulOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulOverrideStatus): Boolean {
        when (status) {
            PendingBackhaulOverrideStatus.PROPOSED -> return next == PendingBackhaulOverrideStatus.REJECTED
            PendingBackhaulOverrideStatus.APPROVED -> return next == PendingBackhaulOverrideStatus.REJECTED
            PendingBackhaulOverrideStatus.REJECTED -> return next == PendingBackhaulOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingBackhaulOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round(durationMinutes.toDouble() * ratio * 100) / 100

    fun snapshot(): PendingBackhaulOverride {
        return PendingBackhaulOverride(
            code = code,
            name = name,
            ratio = ratio,
            durationMinutes = durationMinutes,
            category = category,
            startsAt = startsAt,
            depot = depot,
            description = description,
            reviewer = reviewer,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending backhaul override in status proposed. */
fun newPendingBackhaulOverride(code: String, name: String): PendingBackhaulOverride {
    return PendingBackhaulOverride(
        code = code,
        name = name,
        ratio = 0.0,
        durationMinutes = 0,
        category = "",
        startsAt = 0,
        depot = "",
        description = "",
        reviewer = "",
        visible = false,
        status = PendingBackhaulOverrideStatus.PROPOSED,
    )
}
