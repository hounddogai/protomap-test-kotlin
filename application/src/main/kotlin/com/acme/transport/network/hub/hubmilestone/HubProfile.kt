package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub profile is in its life cycle. */
enum class HubProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A hub profile. Its code is unique within a tenant. */
data class HubProfile(
    var code: String,
    var label: String,
    var unit: String,
    var expiresAt: Int,
    var cost: Double,
    var score: Double,
    var endsAt: Int,
    var status: HubProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubProfileStatus): Boolean {
        when (status) {
            HubProfileStatus.REQUESTED -> {
                when (next) {
                    HubProfileStatus.REVOKED, HubProfileStatus.EXPIRED -> return true
                }
                return false
            }
            HubProfileStatus.GRANTED -> return next == HubProfileStatus.REVOKED
            HubProfileStatus.REVOKED -> {
                when (next) {
                    HubProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubProfileStatus) {
        if (!canMoveTo(next)) {
            error("hub profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"hub profile"} $code"

    fun projectedVolume(): Double = round((expiresAt.toDouble() + cost) * 100) / 100

    fun snapshot(): HubProfile {
        return HubProfile(
            code = code,
            label = label,
            unit = unit,
            expiresAt = expiresAt,
            cost = cost,
            score = score,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a hub profile in status requested. */
fun newHubProfile(code: String, label: String): HubProfile {
    return HubProfile(
        code = code,
        label = label,
        unit = "",
        expiresAt = 0,
        cost = 0.0,
        score = 0.0,
        endsAt = 0,
        status = HubProfileStatus.REQUESTED,
    )
}
