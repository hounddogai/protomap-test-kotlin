package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling hub ledger is in its life cycle. */
enum class RollingHubLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling hub ledger. Its code is unique within a tenant. */
data class RollingHubLedger(
    var code: String,
    var name: String,
    var country: String,
    var threshold: Double,
    var capacity: Int,
    var channel: String,
    var version: Int,
    var weight: Double,
    var quantity: Int,
    var status: RollingHubLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHubLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHubLedgerStatus): Boolean {
        when (status) {
            RollingHubLedgerStatus.PROPOSED -> return next == RollingHubLedgerStatus.APPROVED || next == RollingHubLedgerStatus.REJECTED
            RollingHubLedgerStatus.APPROVED -> {
                when (next) {
                    RollingHubLedgerStatus.REJECTED, RollingHubLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingHubLedgerStatus.REJECTED -> return next == RollingHubLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingHubLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling hub ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round(version.toDouble() * weight * 100) / 100

    fun snapshot(): RollingHubLedger {
        return RollingHubLedger(
            code = code,
            name = name,
            country = country,
            threshold = threshold,
            capacity = capacity,
            channel = channel,
            version = version,
            weight = weight,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling hub ledger in status proposed. */
fun newRollingHubLedger(code: String, name: String): RollingHubLedger {
    return RollingHubLedger(
        code = code,
        name = name,
        country = "",
        threshold = 0.0,
        capacity = 0,
        channel = "",
        version = 0,
        weight = 0.0,
        quantity = 0,
        status = RollingHubLedgerStatus.PROPOSED,
    )
}
