package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tire ledger is in its life cycle. */
enum class ManualTireLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One manual tire ledger, with what the planners need to act on it. */
data class ManualTireLedger(
    var code: String,
    var label: String,
    var ratio: Double,
    var cost: Double,
    var tags: MutableList<String>,
    var status: ManualTireLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTireLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTireLedgerStatus): Boolean {
        when (status) {
            ManualTireLedgerStatus.REQUESTED -> {
                when (next) {
                    ManualTireLedgerStatus.REVOKED -> return true
                }
                return false
            }
            ManualTireLedgerStatus.GRANTED -> return next == ManualTireLedgerStatus.EXPIRED
            ManualTireLedgerStatus.REVOKED -> {
                when (next) {
                    ManualTireLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTireLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual tire ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(ratio * cost * 100) / 100

    fun snapshot(): ManualTireLedger {
        return ManualTireLedger(
            code = code,
            label = label,
            ratio = ratio,
            cost = cost,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual tire ledger in status requested. */
fun newManualTireLedger(code: String, label: String): ManualTireLedger {
    return ManualTireLedger(
        code = code,
        label = label,
        ratio = 0.0,
        cost = 0.0,
        tags = mutableListOf<String>(),
        status = ManualTireLedgerStatus.REQUESTED,
    )
}
