package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy zone bundle records of network's zone work, and the rules, stores, and calculations around them.

/** Where a legacy zone bundle is in its life cycle. */
enum class LegacyZoneBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy zone bundle. Its code is unique within a tenant. */
data class LegacyZoneBundle(
    var code: String,
    var name: String,
    var budget: Double,
    var region: String,
    var channel: String,
    var capacity: Int,
    var country: String,
    var status: LegacyZoneBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneBundleStatus): Boolean {
        when (status) {
            LegacyZoneBundleStatus.PROPOSED -> {
                when (next) {
                    LegacyZoneBundleStatus.APPROVED, LegacyZoneBundleStatus.REJECTED -> return true
                }
                return false
            }
            LegacyZoneBundleStatus.APPROVED -> {
                when (next) {
                    LegacyZoneBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyZoneBundleStatus.REJECTED -> return next == LegacyZoneBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyZoneBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((capacity.toDouble() - budget) * 100) / 100

    fun snapshot(): LegacyZoneBundle {
        return LegacyZoneBundle(
            code = code,
            name = name,
            budget = budget,
            region = region,
            channel = channel,
            capacity = capacity,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy zone bundle in status proposed. */
fun newLegacyZoneBundle(code: String, name: String): LegacyZoneBundle {
    return LegacyZoneBundle(
        code = code,
        name = name,
        budget = 0.0,
        region = "",
        channel = "",
        capacity = 0,
        country = "",
        status = LegacyZoneBundleStatus.PROPOSED,
    )
}
