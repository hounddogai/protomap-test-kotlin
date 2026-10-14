package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay override is in its life cycle. */
enum class RelayOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One relay override, with what the planners need to act on it. */
data class RelayOverride(
    var code: String,
    var name: String,
    var ownerId: String,
    var active: Boolean,
    var limit: Int,
    var discount: Double,
    var ceiling: Double,
    var batchSize: Int,
    var weight: Double,
    var status: RelayOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayOverrideStatus): Boolean {
        when (status) {
            RelayOverrideStatus.OPEN -> {
                when (next) {
                    RelayOverrideStatus.IN_REVIEW -> return true
                }
                return false
            }
            RelayOverrideStatus.IN_REVIEW -> return next == RelayOverrideStatus.ARCHIVED
            RelayOverrideStatus.RESOLVED -> return next == RelayOverrideStatus.REOPENED
            RelayOverrideStatus.REOPENED -> return next == RelayOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RelayOverrideStatus) {
        if (!canMoveTo(next)) {
            error("relay override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round(limit.toDouble() * weight * 100) / 100

    fun snapshot(): RelayOverride {
        return RelayOverride(
            code = code,
            name = name,
            ownerId = ownerId,
            active = active,
            limit = limit,
            discount = discount,
            ceiling = ceiling,
            batchSize = batchSize,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay override in status open. */
fun newRelayOverride(code: String, name: String): RelayOverride {
    return RelayOverride(
        code = code,
        name = name,
        ownerId = "",
        active = false,
        limit = 0,
        discount = 0.0,
        ceiling = 0.0,
        batchSize = 0,
        weight = 0.0,
        status = RelayOverrideStatus.OPEN,
    )
}
