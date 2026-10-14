package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay claim is in its life cycle. */
enum class RelayClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a relay claim and its current status. */
data class RelayClaim(
    var code: String,
    var title: String,
    var category: String,
    var maximum: Double,
    var label: String,
    var loadKg: Double,
    var retries: Int,
    var driverId: String,
    var updatedAt: Int,
    var status: RelayClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayClaimStatus): Boolean {
        when (status) {
            RelayClaimStatus.DRAFT -> {
                when (next) {
                    RelayClaimStatus.ACTIVE -> return true
                }
                return false
            }
            RelayClaimStatus.PENDING -> return next == RelayClaimStatus.ACTIVE
            RelayClaimStatus.ACTIVE -> {
                when (next) {
                    RelayClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            RelayClaimStatus.SUSPENDED -> {
                when (next) {
                    RelayClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayClaimStatus) {
        if (!canMoveTo(next)) {
            error("relay claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedVolume(): Double = round(updatedAt.toDouble() * maximum * 100) / 100

    fun snapshot(): RelayClaim {
        return RelayClaim(
            code = code,
            title = title,
            category = category,
            maximum = maximum,
            label = label,
            loadKg = loadKg,
            retries = retries,
            driverId = driverId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a relay claim in status draft. */
fun newRelayClaim(code: String, title: String): RelayClaim {
    return RelayClaim(
        code = code,
        title = title,
        category = "",
        maximum = 0.0,
        label = "",
        loadKg = 0.0,
        retries = 0,
        driverId = "",
        updatedAt = 0,
        status = RelayClaimStatus.DRAFT,
    )
}
