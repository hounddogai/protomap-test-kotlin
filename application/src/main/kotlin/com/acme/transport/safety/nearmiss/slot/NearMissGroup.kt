package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss group is in its life cycle. */
enum class NearMissGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One near miss group, with what the planners need to act on it. */
data class NearMissGroup(
    var code: String,
    var title: String,
    var maximum: Double,
    var approved: Boolean,
    var pinned: Boolean,
    var etaMinutes: Int,
    var routeCode: String,
    var description: String,
    var weight: Double,
    var status: NearMissGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissGroupStatus): Boolean {
        when (status) {
            NearMissGroupStatus.REQUESTED -> return next == NearMissGroupStatus.GRANTED || next == NearMissGroupStatus.EXPIRED
            NearMissGroupStatus.GRANTED -> return next == NearMissGroupStatus.EXPIRED
            NearMissGroupStatus.REVOKED -> return next == NearMissGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: NearMissGroupStatus) {
        if (!canMoveTo(next)) {
            error("near miss group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss group"} $code"

    fun effectiveTotal(): Double = round((etaMinutes.toDouble() + weight) * 100) / 100

    fun snapshot(): NearMissGroup {
        return NearMissGroup(
            code = code,
            title = title,
            maximum = maximum,
            approved = approved,
            pinned = pinned,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            description = description,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss group in status requested. */
fun newNearMissGroup(code: String, title: String): NearMissGroup {
    return NearMissGroup(
        code = code,
        title = title,
        maximum = 0.0,
        approved = false,
        pinned = false,
        etaMinutes = 0,
        routeCode = "",
        description = "",
        weight = 0.0,
        status = NearMissGroupStatus.REQUESTED,
    )
}
