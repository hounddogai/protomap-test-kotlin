package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub ledger is in its life cycle. */
enum class HubLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One hub ledger, with what the planners need to act on it. */
data class HubLedger(
    var code: String,
    var name: String,
    var weight: Double,
    var channel: String,
    var capacity: Int,
    var phone: String,
    var status: HubLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubLedgerStatus): Boolean {
        when (status) {
            HubLedgerStatus.DRAFT -> return next == HubLedgerStatus.ACTIVE
            HubLedgerStatus.PENDING -> {
                when (next) {
                    HubLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            HubLedgerStatus.ACTIVE -> return next == HubLedgerStatus.SUSPENDED || next == HubLedgerStatus.CLOSED
            HubLedgerStatus.SUSPENDED -> {
                when (next) {
                    HubLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubLedgerStatus) {
        if (!canMoveTo(next)) {
            error("hub ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hub ledger"} $code"

    fun netLoad(): Double = round((capacity.toDouble() + weight) * 100) / 100

    fun snapshot(): HubLedger {
        return HubLedger(
            code = code,
            name = name,
            weight = weight,
            channel = channel,
            capacity = capacity,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hub ledger in status draft. */
fun newHubLedger(code: String, name: String): HubLedger {
    return HubLedger(
        code = code,
        name = name,
        weight = 0.0,
        channel = "",
        capacity = 0,
        phone = "",
        status = HubLedgerStatus.DRAFT,
    )
}
