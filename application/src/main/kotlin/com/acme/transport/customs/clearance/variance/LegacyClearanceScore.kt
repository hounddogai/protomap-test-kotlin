package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy clearance score is in its life cycle. */
enum class LegacyClearanceScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy clearance score. Its code is unique within a tenant. */
data class LegacyClearanceScore(
    var code: String,
    var label: String,
    var driverId: String,
    var pallets: Int,
    var createdAt: Int,
    var status: LegacyClearanceScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceScoreStatus): Boolean {
        when (status) {
            LegacyClearanceScoreStatus.PROPOSED -> {
                when (next) {
                    LegacyClearanceScoreStatus.REJECTED, LegacyClearanceScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyClearanceScoreStatus.APPROVED -> return next == LegacyClearanceScoreStatus.REJECTED || next == LegacyClearanceScoreStatus.WITHDRAWN
            LegacyClearanceScoreStatus.REJECTED -> {
                when (next) {
                    LegacyClearanceScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyClearanceScoreStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round(createdAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): LegacyClearanceScore {
        return LegacyClearanceScore(
            code = code,
            label = label,
            driverId = driverId,
            pallets = pallets,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy clearance score in status proposed. */
fun newLegacyClearanceScore(code: String, label: String): LegacyClearanceScore {
    return LegacyClearanceScore(
        code = code,
        label = label,
        driverId = "",
        pallets = 0,
        createdAt = 0,
        status = LegacyClearanceScoreStatus.PROPOSED,
    )
}
