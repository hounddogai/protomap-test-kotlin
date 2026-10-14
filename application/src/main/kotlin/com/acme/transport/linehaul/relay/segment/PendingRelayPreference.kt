package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay preference is in its life cycle. */
enum class PendingRelayPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending relay preference, with what the planners need to act on it. */
data class PendingRelayPreference(
    var code: String,
    var name: String,
    var timezone: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var cost: Double,
    var status: PendingRelayPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayPreferenceStatus): Boolean {
        when (status) {
            PendingRelayPreferenceStatus.CREATED -> {
                when (next) {
                    PendingRelayPreferenceStatus.VALIDATED, PendingRelayPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            PendingRelayPreferenceStatus.VALIDATED -> {
                when (next) {
                    PendingRelayPreferenceStatus.PUBLISHED, PendingRelayPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            PendingRelayPreferenceStatus.PUBLISHED -> {
                when (next) {
                    PendingRelayPreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelayPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending relay preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(createdAt.toDouble() * cost * 100) / 100

    fun snapshot(): PendingRelayPreference {
        return PendingRelayPreference(
            code = code,
            name = name,
            timezone = timezone,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending relay preference in status created. */
fun newPendingRelayPreference(code: String, name: String): PendingRelayPreference {
    return PendingRelayPreference(
        code = code,
        name = name,
        timezone = "",
        durationMinutes = 0,
        createdAt = 0,
        cost = 0.0,
        status = PendingRelayPreferenceStatus.CREATED,
    )
}
