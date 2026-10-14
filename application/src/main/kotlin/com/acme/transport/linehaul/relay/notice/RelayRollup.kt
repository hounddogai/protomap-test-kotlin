package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay rollup is in its life cycle. */
enum class RelayRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A relay rollup as the relay team keeps it. */
data class RelayRollup(
    var code: String,
    var title: String,
    var version: Int,
    var description: String,
    var pallets: Int,
    var attempts: Int,
    var locale: String,
    var expiresAt: Int,
    var budget: Double,
    var ratio: Double,
    var ownerId: String,
    var status: RelayRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayRollupStatus): Boolean {
        when (status) {
            RelayRollupStatus.OPEN -> {
                when (next) {
                    RelayRollupStatus.REOPENED -> return true
                }
                return false
            }
            RelayRollupStatus.IN_REVIEW -> return next == RelayRollupStatus.REOPENED
            RelayRollupStatus.RESOLVED -> {
                when (next) {
                    RelayRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayRollupStatus.REOPENED -> {
                when (next) {
                    RelayRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayRollupStatus) {
        if (!canMoveTo(next)) {
            error("relay rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((budget - attempts.toDouble()) * 100) / 100

    fun snapshot(): RelayRollup {
        return RelayRollup(
            code = code,
            title = title,
            version = version,
            description = description,
            pallets = pallets,
            attempts = attempts,
            locale = locale,
            expiresAt = expiresAt,
            budget = budget,
            ratio = ratio,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a relay rollup in status open. */
fun newRelayRollup(code: String, title: String): RelayRollup {
    return RelayRollup(
        code = code,
        title = title,
        version = 0,
        description = "",
        pallets = 0,
        attempts = 0,
        locale = "",
        expiresAt = 0,
        budget = 0.0,
        ratio = 0.0,
        ownerId = "",
        status = RelayRollupStatus.OPEN,
    )
}
