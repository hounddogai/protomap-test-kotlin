package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default zone group records of network's zone work, and the rules, stores, and calculations around them.

/** Where a default zone group is in its life cycle. */
enum class DefaultZoneGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default zone group, with what the planners need to act on it. */
data class DefaultZoneGroup(
    var code: String,
    var title: String,
    var ownerId: String,
    var shiftCode: String,
    var expiresAt: Int,
    var endsAt: Int,
    var minimum: Double,
    var depot: String,
    var retries: Int,
    var loadKg: Double,
    var status: DefaultZoneGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultZoneGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultZoneGroupStatus): Boolean {
        when (status) {
            DefaultZoneGroupStatus.REQUESTED -> {
                when (next) {
                    DefaultZoneGroupStatus.REVOKED -> return true
                }
                return false
            }
            DefaultZoneGroupStatus.GRANTED -> {
                when (next) {
                    DefaultZoneGroupStatus.REVOKED -> return true
                }
                return false
            }
            DefaultZoneGroupStatus.REVOKED -> return next == DefaultZoneGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultZoneGroupStatus) {
        if (!canMoveTo(next)) {
            error("default zone group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default zone group"} $code"

    fun projectedTotal(): Double = round((retries.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultZoneGroup {
        return DefaultZoneGroup(
            code = code,
            title = title,
            ownerId = ownerId,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            endsAt = endsAt,
            minimum = minimum,
            depot = depot,
            retries = retries,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default zone group in status requested. */
fun newDefaultZoneGroup(code: String, title: String): DefaultZoneGroup {
    return DefaultZoneGroup(
        code = code,
        title = title,
        ownerId = "",
        shiftCode = "",
        expiresAt = 0,
        endsAt = 0,
        minimum = 0.0,
        depot = "",
        retries = 0,
        loadKg = 0.0,
        status = DefaultZoneGroupStatus.REQUESTED,
    )
}
