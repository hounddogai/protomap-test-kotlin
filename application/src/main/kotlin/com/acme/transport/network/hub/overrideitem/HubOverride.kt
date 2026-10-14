package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub override records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub override is in its life cycle. */
enum class HubOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hub override. Its code is unique within a tenant. */
data class HubOverride(
    var code: String,
    var title: String,
    var createdAt: Int,
    var expiresAt: Int,
    var timezone: String,
    var currency: String,
    var description: String,
    var pallets: Int,
    var name: String,
    var lane: String,
    var loadKg: Double,
    var status: HubOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubOverrideStatus): Boolean {
        when (status) {
            HubOverrideStatus.DRAFT -> {
                when (next) {
                    HubOverrideStatus.CLOSED -> return true
                }
                return false
            }
            HubOverrideStatus.PENDING -> return next == HubOverrideStatus.SUSPENDED
            HubOverrideStatus.ACTIVE -> {
                when (next) {
                    HubOverrideStatus.SUSPENDED, HubOverrideStatus.CLOSED -> return true
                }
                return false
            }
            HubOverrideStatus.SUSPENDED -> return next == HubOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HubOverrideStatus) {
        if (!canMoveTo(next)) {
            error("hub override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): HubOverride {
        return HubOverride(
            code = code,
            title = title,
            createdAt = createdAt,
            expiresAt = expiresAt,
            timezone = timezone,
            currency = currency,
            description = description,
            pallets = pallets,
            name = name,
            lane = lane,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hub override in status draft. */
fun newHubOverride(code: String, title: String): HubOverride {
    return HubOverride(
        code = code,
        title = title,
        createdAt = 0,
        expiresAt = 0,
        timezone = "",
        currency = "",
        description = "",
        pallets = 0,
        name = "",
        lane = "",
        loadKg = 0.0,
        status = HubOverrideStatus.DRAFT,
    )
}
