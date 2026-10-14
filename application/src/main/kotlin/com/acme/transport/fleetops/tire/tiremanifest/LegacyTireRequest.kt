package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize

/** Where a legacy tire request is in its life cycle. */
enum class LegacyTireRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy tire request. Its code is unique within a tenant. */
data class LegacyTireRequest(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var timezone: String,
    var createdAt: Int,
    var status: LegacyTireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireRequestStatus): Boolean {
        when (status) {
            LegacyTireRequestStatus.REQUESTED -> return next == LegacyTireRequestStatus.GRANTED || next == LegacyTireRequestStatus.REVOKED
            LegacyTireRequestStatus.GRANTED -> return next == LegacyTireRequestStatus.REVOKED
            LegacyTireRequestStatus.REVOKED -> {
                when (next) {
                    LegacyTireRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTireRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): LegacyTireRequest {
        return LegacyTireRequest(
            code = code,
            name = name,
            pinned = pinned,
            timezone = timezone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy tire request in status requested. */
fun newLegacyTireRequest(code: String, name: String): LegacyTireRequest {
    return LegacyTireRequest(
        code = code,
        name = name,
        pinned = false,
        timezone = "",
        createdAt = 0,
        status = LegacyTireRequestStatus.REQUESTED,
    )
}
