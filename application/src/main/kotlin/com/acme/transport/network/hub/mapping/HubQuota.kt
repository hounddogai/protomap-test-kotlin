package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub quota is in its life cycle. */
enum class HubQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A hub quota. Its code is unique within a tenant. */
data class HubQuota(
    var code: String,
    var name: String,
    var active: Boolean,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var expiresAt: Int,
    var checksum: String,
    var graceDays: Int,
    var status: HubQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubQuotaStatus): Boolean {
        when (status) {
            HubQuotaStatus.REQUESTED -> return next == HubQuotaStatus.REVOKED || next == HubQuotaStatus.EXPIRED
            HubQuotaStatus.GRANTED -> {
                when (next) {
                    HubQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            HubQuotaStatus.REVOKED -> {
                when (next) {
                    HubQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubQuotaStatus) {
        if (!canMoveTo(next)) {
            error("hub quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((graceDays.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): HubQuota {
        return HubQuota(
            code = code,
            name = name,
            active = active,
            timezone = timezone,
            attributes = attributes,
            endsAt = endsAt,
            expiresAt = expiresAt,
            checksum = checksum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub quota in status requested. */
fun newHubQuota(code: String, name: String): HubQuota {
    return HubQuota(
        code = code,
        name = name,
        active = false,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        expiresAt = 0,
        checksum = "",
        graceDays = 0,
        status = HubQuotaStatus.REQUESTED,
    )
}
