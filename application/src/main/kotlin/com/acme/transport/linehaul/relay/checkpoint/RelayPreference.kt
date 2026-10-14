package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay preference is in its life cycle. */
enum class RelayPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One relay preference, with what the planners need to act on it. */
data class RelayPreference(
    var code: String,
    var label: String,
    var routeCode: String,
    var score: Double,
    var currency: String,
    var startsAt: Int,
    var status: RelayPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayPreferenceStatus): Boolean {
        when (status) {
            RelayPreferenceStatus.REQUESTED -> {
                when (next) {
                    RelayPreferenceStatus.GRANTED -> return true
                }
                return false
            }
            RelayPreferenceStatus.GRANTED -> {
                when (next) {
                    RelayPreferenceStatus.REVOKED, RelayPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            RelayPreferenceStatus.REVOKED -> {
                when (next) {
                    RelayPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("relay preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((score + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RelayPreference {
        return RelayPreference(
            code = code,
            label = label,
            routeCode = routeCode,
            score = score,
            currency = currency,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay preference in status requested. */
fun newRelayPreference(code: String, label: String): RelayPreference {
    return RelayPreference(
        code = code,
        label = label,
        routeCode = "",
        score = 0.0,
        currency = "",
        startsAt = 0,
        status = RelayPreferenceStatus.REQUESTED,
    )
}
