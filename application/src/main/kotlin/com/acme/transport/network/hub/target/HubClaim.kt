package com.acme.transport.network.hub.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub claim is in its life cycle. */
enum class HubClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hub claim as the hub team keeps it. */
data class HubClaim(
    var code: String,
    var name: String,
    var batchSize: Int,
    var limit: Int,
    var summary: String,
    var unit: String,
    var status: HubClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubClaimStatus): Boolean {
        when (status) {
            HubClaimStatus.DRAFT -> {
                when (next) {
                    HubClaimStatus.ACTIVE, HubClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            HubClaimStatus.PENDING -> {
                when (next) {
                    HubClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            HubClaimStatus.ACTIVE -> {
                when (next) {
                    HubClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            HubClaimStatus.SUSPENDED -> return next == HubClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HubClaimStatus) {
        if (!canMoveTo(next)) {
            error("hub claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(limit.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): HubClaim {
        return HubClaim(
            code = code,
            name = name,
            batchSize = batchSize,
            limit = limit,
            summary = summary,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a hub claim in status draft. */
fun newHubClaim(code: String, name: String): HubClaim {
    return HubClaim(
        code = code,
        name = name,
        batchSize = 0,
        limit = 0,
        summary = "",
        unit = "",
        status = HubClaimStatus.DRAFT,
    )
}
