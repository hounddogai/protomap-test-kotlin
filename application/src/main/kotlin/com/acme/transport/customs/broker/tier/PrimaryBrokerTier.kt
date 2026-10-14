package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary broker tier records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a primary broker tier is in its life cycle. */
enum class PrimaryBrokerTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary broker tier as the broker team keeps it. */
data class PrimaryBrokerTier(
    var code: String,
    var label: String,
    var checksum: String,
    var updatedAt: Int,
    var priority: Int,
    var etaMinutes: Int,
    var createdAt: Int,
    var status: PrimaryBrokerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBrokerTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBrokerTierStatus): Boolean {
        when (status) {
            PrimaryBrokerTierStatus.REQUESTED -> return next == PrimaryBrokerTierStatus.GRANTED
            PrimaryBrokerTierStatus.GRANTED -> return next == PrimaryBrokerTierStatus.REVOKED
            PrimaryBrokerTierStatus.REVOKED -> {
                when (next) {
                    PrimaryBrokerTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryBrokerTierStatus) {
        if (!canMoveTo(next)) {
            error("primary broker tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((updatedAt.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryBrokerTier {
        return PrimaryBrokerTier(
            code = code,
            label = label,
            checksum = checksum,
            updatedAt = updatedAt,
            priority = priority,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary broker tier in status requested. */
fun newPrimaryBrokerTier(code: String, label: String): PrimaryBrokerTier {
    return PrimaryBrokerTier(
        code = code,
        label = label,
        checksum = "",
        updatedAt = 0,
        priority = 0,
        etaMinutes = 0,
        createdAt = 0,
        status = PrimaryBrokerTierStatus.REQUESTED,
    )
}
