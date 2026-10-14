package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay quota records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay quota is in its life cycle. */
enum class RelayQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One relay quota, with what the planners need to act on it. */
data class RelayQuota(
    var code: String,
    var name: String,
    var segment: String,
    var priority: Int,
    var expiresAt: Int,
    var status: RelayQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayQuotaStatus): Boolean {
        when (status) {
            RelayQuotaStatus.DRAFT -> return next == RelayQuotaStatus.SUSPENDED || next == RelayQuotaStatus.CLOSED
            RelayQuotaStatus.PENDING -> {
                when (next) {
                    RelayQuotaStatus.ACTIVE -> return true
                }
                return false
            }
            RelayQuotaStatus.ACTIVE -> {
                when (next) {
                    RelayQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            RelayQuotaStatus.SUSPENDED -> return next == RelayQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RelayQuotaStatus) {
        if (!canMoveTo(next)) {
            error("relay quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((priority.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RelayQuota {
        return RelayQuota(
            code = code,
            name = name,
            segment = segment,
            priority = priority,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a relay quota in status draft. */
fun newRelayQuota(code: String, name: String): RelayQuota {
    return RelayQuota(
        code = code,
        name = name,
        segment = "",
        priority = 0,
        expiresAt = 0,
        status = RelayQuotaStatus.DRAFT,
    )
}
