package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lease return rollup is in its life cycle. */
enum class CappedLeaseReturnRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped lease return rollup as the lease return team keeps it. */
data class CappedLeaseReturnRollup(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var discount: Double,
    var status: CappedLeaseReturnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLeaseReturnRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLeaseReturnRollupStatus): Boolean {
        when (status) {
            CappedLeaseReturnRollupStatus.PROPOSED -> {
                when (next) {
                    CappedLeaseReturnRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedLeaseReturnRollupStatus.APPROVED -> return next == CappedLeaseReturnRollupStatus.WITHDRAWN
            CappedLeaseReturnRollupStatus.REJECTED -> {
                when (next) {
                    CappedLeaseReturnRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLeaseReturnRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped lease return rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped lease return rollup"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((rate + discount) * 100) / 100

    fun snapshot(): CappedLeaseReturnRollup {
        return CappedLeaseReturnRollup(
            code = code,
            label = label,
            attributes = attributes,
            rate = rate,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped lease return rollup in status proposed. */
fun newCappedLeaseReturnRollup(code: String, label: String): CappedLeaseReturnRollup {
    return CappedLeaseReturnRollup(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        discount = 0.0,
        status = CappedLeaseReturnRollupStatus.PROPOSED,
    )
}
