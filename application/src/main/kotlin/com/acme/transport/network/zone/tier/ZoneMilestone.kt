package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.normalize

/** Where a zone milestone is in its life cycle. */
enum class ZoneMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A zone milestone. Its code is unique within a tenant. */
data class ZoneMilestone(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var currency: String,
    var contactEmail: String,
    var trailerId: String,
    var region: String,
    var rate: Double,
    var shiftCode: String,
    var status: ZoneMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneMilestoneStatus): Boolean {
        when (status) {
            ZoneMilestoneStatus.PROPOSED -> return next == ZoneMilestoneStatus.APPROVED || next == ZoneMilestoneStatus.REJECTED
            ZoneMilestoneStatus.APPROVED -> {
                when (next) {
                    ZoneMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            ZoneMilestoneStatus.REJECTED -> return next == ZoneMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ZoneMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("zone milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"zone milestone"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = rate

    fun snapshot(): ZoneMilestone {
        return ZoneMilestone(
            code = code,
            title = title,
            attributes = attributes,
            channel = channel,
            currency = currency,
            contactEmail = contactEmail,
            trailerId = trailerId,
            region = region,
            rate = rate,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a zone milestone in status proposed. */
fun newZoneMilestone(code: String, title: String): ZoneMilestone {
    return ZoneMilestone(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        currency = "",
        contactEmail = "",
        trailerId = "",
        region = "",
        rate = 0.0,
        shiftCode = "",
        status = ZoneMilestoneStatus.PROPOSED,
    )
}
