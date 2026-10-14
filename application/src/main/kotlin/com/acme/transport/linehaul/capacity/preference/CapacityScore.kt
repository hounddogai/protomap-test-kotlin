package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity score is in its life cycle. */
enum class CapacityScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capacity score as the capacity team keeps it. */
data class CapacityScore(
    var code: String,
    var label: String,
    var segment: String,
    var etaMinutes: Int,
    var ownerId: String,
    var expiresAt: Int,
    var status: CapacityScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityScoreStatus): Boolean {
        when (status) {
            CapacityScoreStatus.PROPOSED -> {
                when (next) {
                    CapacityScoreStatus.APPROVED, CapacityScoreStatus.REJECTED -> return true
                }
                return false
            }
            CapacityScoreStatus.APPROVED -> return next == CapacityScoreStatus.REJECTED
            CapacityScoreStatus.REJECTED -> {
                when (next) {
                    CapacityScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityScoreStatus) {
        if (!canMoveTo(next)) {
            error("capacity score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((expiresAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CapacityScore {
        return CapacityScore(
            code = code,
            label = label,
            segment = segment,
            etaMinutes = etaMinutes,
            ownerId = ownerId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capacity score in status proposed. */
fun newCapacityScore(code: String, label: String): CapacityScore {
    return CapacityScore(
        code = code,
        label = label,
        segment = "",
        etaMinutes = 0,
        ownerId = "",
        expiresAt = 0,
        status = CapacityScoreStatus.PROPOSED,
    )
}
