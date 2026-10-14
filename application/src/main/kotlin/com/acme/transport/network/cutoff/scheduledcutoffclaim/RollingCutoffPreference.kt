package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling cutoff preference is in its life cycle. */
enum class RollingCutoffPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling cutoff preference as the cutoff team keeps it. */
data class RollingCutoffPreference(
    var code: String,
    var name: String,
    var startsAt: Int,
    var label: String,
    var capacity: Int,
    var shiftCode: String,
    var cost: Double,
    var status: RollingCutoffPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffPreferenceStatus): Boolean {
        when (status) {
            RollingCutoffPreferenceStatus.PROPOSED -> {
                when (next) {
                    RollingCutoffPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingCutoffPreferenceStatus.APPROVED -> return next == RollingCutoffPreferenceStatus.WITHDRAWN
            RollingCutoffPreferenceStatus.REJECTED -> return next == RollingCutoffPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingCutoffPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun adjustedVolume(): Double = round((cost + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingCutoffPreference {
        return RollingCutoffPreference(
            code = code,
            name = name,
            startsAt = startsAt,
            label = label,
            capacity = capacity,
            shiftCode = shiftCode,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling cutoff preference in status proposed. */
fun newRollingCutoffPreference(code: String, name: String): RollingCutoffPreference {
    return RollingCutoffPreference(
        code = code,
        name = name,
        startsAt = 0,
        label = "",
        capacity = 0,
        shiftCode = "",
        cost = 0.0,
        status = RollingCutoffPreferenceStatus.PROPOSED,
    )
}
