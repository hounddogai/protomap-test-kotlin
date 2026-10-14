package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dispute slot is in its life cycle. */
enum class LegacyDisputeSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy dispute slot as the dispute team keeps it. */
data class LegacyDisputeSlot(
    var code: String,
    var title: String,
    var stopCount: Int,
    var depot: String,
    var budget: Double,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var currency: String,
    var description: String,
    var status: LegacyDisputeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDisputeSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDisputeSlotStatus): Boolean {
        when (status) {
            LegacyDisputeSlotStatus.IDLE -> {
                when (next) {
                    LegacyDisputeSlotStatus.DRAINING -> return true
                }
                return false
            }
            LegacyDisputeSlotStatus.WARMING -> return next == LegacyDisputeSlotStatus.READY || next == LegacyDisputeSlotStatus.DRAINING
            LegacyDisputeSlotStatus.READY -> {
                when (next) {
                    LegacyDisputeSlotStatus.DRAINING, LegacyDisputeSlotStatus.STOPPED -> return true
                }
                return false
            }
            LegacyDisputeSlotStatus.DRAINING -> return next == LegacyDisputeSlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyDisputeSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy dispute slot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy dispute slot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(budget * expiresAt.toDouble() * 100) / 100

    fun snapshot(): LegacyDisputeSlot {
        return LegacyDisputeSlot(
            code = code,
            title = title,
            stopCount = stopCount,
            depot = depot,
            budget = budget,
            expiresAt = expiresAt,
            attributes = attributes,
            currency = currency,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy dispute slot in status idle. */
fun newLegacyDisputeSlot(code: String, title: String): LegacyDisputeSlot {
    return LegacyDisputeSlot(
        code = code,
        title = title,
        stopCount = 0,
        depot = "",
        budget = 0.0,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        currency = "",
        description = "",
        status = LegacyDisputeSlotStatus.IDLE,
    )
}
