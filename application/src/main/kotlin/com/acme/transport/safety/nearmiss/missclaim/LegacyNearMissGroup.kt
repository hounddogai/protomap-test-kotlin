package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.text.normalize

/** Where a legacy near miss group is in its life cycle. */
enum class LegacyNearMissGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy near miss group. Its code is unique within a tenant. */
data class LegacyNearMissGroup(
    var code: String,
    var title: String,
    var carrier: String,
    var limit: Int,
    var timezone: String,
    var shiftCode: String,
    var status: LegacyNearMissGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNearMissGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNearMissGroupStatus): Boolean {
        when (status) {
            LegacyNearMissGroupStatus.REQUESTED -> return next == LegacyNearMissGroupStatus.GRANTED
            LegacyNearMissGroupStatus.GRANTED -> {
                when (next) {
                    LegacyNearMissGroupStatus.REVOKED -> return true
                }
                return false
            }
            LegacyNearMissGroupStatus.REVOKED -> {
                when (next) {
                    LegacyNearMissGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyNearMissGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy near miss group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): LegacyNearMissGroup {
        return LegacyNearMissGroup(
            code = code,
            title = title,
            carrier = carrier,
            limit = limit,
            timezone = timezone,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy near miss group in status requested. */
fun newLegacyNearMissGroup(code: String, title: String): LegacyNearMissGroup {
    return LegacyNearMissGroup(
        code = code,
        title = title,
        carrier = "",
        limit = 0,
        timezone = "",
        shiftCode = "",
        status = LegacyNearMissGroupStatus.REQUESTED,
    )
}
