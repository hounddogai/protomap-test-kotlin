package com.acme.transport.network.hub.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global hub ledger is in its life cycle. */
enum class GlobalHubLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a global hub ledger and its current status. */
data class GlobalHubLedger(
    var code: String,
    var name: String,
    var batchSize: Int,
    var loadKg: Double,
    var createdAt: Int,
    var country: String,
    var quantity: Int,
    var approved: Boolean,
    var startsAt: Int,
    var region: String,
    var status: GlobalHubLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHubLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHubLedgerStatus): Boolean {
        when (status) {
            GlobalHubLedgerStatus.IDLE -> return next == GlobalHubLedgerStatus.STOPPED
            GlobalHubLedgerStatus.WARMING -> {
                when (next) {
                    GlobalHubLedgerStatus.READY, GlobalHubLedgerStatus.DRAINING -> return true
                }
                return false
            }
            GlobalHubLedgerStatus.READY -> {
                when (next) {
                    GlobalHubLedgerStatus.DRAINING -> return true
                }
                return false
            }
            GlobalHubLedgerStatus.DRAINING -> return next == GlobalHubLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalHubLedgerStatus) {
        if (!canMoveTo(next)) {
            error("global hub ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round(batchSize.toDouble() * loadKg * 100) / 100

    fun snapshot(): GlobalHubLedger {
        return GlobalHubLedger(
            code = code,
            name = name,
            batchSize = batchSize,
            loadKg = loadKg,
            createdAt = createdAt,
            country = country,
            quantity = quantity,
            approved = approved,
            startsAt = startsAt,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global hub ledger in status idle. */
fun newGlobalHubLedger(code: String, name: String): GlobalHubLedger {
    return GlobalHubLedger(
        code = code,
        name = name,
        batchSize = 0,
        loadKg = 0.0,
        createdAt = 0,
        country = "",
        quantity = 0,
        approved = false,
        startsAt = 0,
        region = "",
        status = GlobalHubLedgerStatus.IDLE,
    )
}
