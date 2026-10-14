package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling dispute ledger records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a rolling dispute ledger is in its life cycle. */
enum class RollingDisputeLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling dispute ledger as the dispute team keeps it. */
data class RollingDisputeLedger(
    var code: String,
    var name: String,
    var visible: Boolean,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var checksum: String,
    var capacity: Int,
    var description: String,
    var status: RollingDisputeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputeLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputeLedgerStatus): Boolean {
        when (status) {
            RollingDisputeLedgerStatus.OPEN -> {
                when (next) {
                    RollingDisputeLedgerStatus.REOPENED, RollingDisputeLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingDisputeLedgerStatus.IN_REVIEW -> {
                when (next) {
                    RollingDisputeLedgerStatus.RESOLVED -> return true
                }
                return false
            }
            RollingDisputeLedgerStatus.RESOLVED -> return next == RollingDisputeLedgerStatus.REOPENED || next == RollingDisputeLedgerStatus.ARCHIVED
            RollingDisputeLedgerStatus.REOPENED -> return next == RollingDisputeLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingDisputeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((endsAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): RollingDisputeLedger {
        return RollingDisputeLedger(
            code = code,
            name = name,
            visible = visible,
            endsAt = endsAt,
            attributes = attributes,
            pallets = pallets,
            checksum = checksum,
            capacity = capacity,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling dispute ledger in status open. */
fun newRollingDisputeLedger(code: String, name: String): RollingDisputeLedger {
    return RollingDisputeLedger(
        code = code,
        name = name,
        visible = false,
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        checksum = "",
        capacity = 0,
        description = "",
        status = RollingDisputeLedgerStatus.OPEN,
    )
}
