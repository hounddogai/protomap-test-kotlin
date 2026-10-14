package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay digest is in its life cycle. */
enum class RelayDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A relay digest as the relay team keeps it. */
data class RelayDigest(
    var code: String,
    var name: String,
    var archived: Boolean,
    var version: Int,
    var windowDays: Int,
    var odometer: Int,
    var driverId: String,
    var status: RelayDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayDigestStatus): Boolean {
        when (status) {
            RelayDigestStatus.PROPOSED -> {
                when (next) {
                    RelayDigestStatus.APPROVED -> return true
                }
                return false
            }
            RelayDigestStatus.APPROVED -> {
                when (next) {
                    RelayDigestStatus.REJECTED, RelayDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            RelayDigestStatus.REJECTED -> {
                when (next) {
                    RelayDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayDigestStatus) {
        if (!canMoveTo(next)) {
            error("relay digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(windowDays.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): RelayDigest {
        return RelayDigest(
            code = code,
            name = name,
            archived = archived,
            version = version,
            windowDays = windowDays,
            odometer = odometer,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay digest in status proposed. */
fun newRelayDigest(code: String, name: String): RelayDigest {
    return RelayDigest(
        code = code,
        name = name,
        archived = false,
        version = 0,
        windowDays = 0,
        odometer = 0,
        driverId = "",
        status = RelayDigestStatus.PROPOSED,
    )
}
