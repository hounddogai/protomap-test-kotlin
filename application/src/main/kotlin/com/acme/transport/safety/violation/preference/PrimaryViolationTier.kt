package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary violation tier is in its life cycle. */
enum class PrimaryViolationTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary violation tier, with what the planners need to act on it. */
data class PrimaryViolationTier(
    var code: String,
    var label: String,
    var segment: String,
    var loadKg: Double,
    var graceDays: Int,
    var status: PrimaryViolationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryViolationTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryViolationTierStatus): Boolean {
        when (status) {
            PrimaryViolationTierStatus.REQUESTED -> return next == PrimaryViolationTierStatus.EXPIRED
            PrimaryViolationTierStatus.GRANTED -> {
                when (next) {
                    PrimaryViolationTierStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryViolationTierStatus.REVOKED -> return next == PrimaryViolationTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryViolationTierStatus) {
        if (!canMoveTo(next)) {
            error("primary violation tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary violation tier"} $code"

    fun weightedValue(): Double = round(graceDays.toDouble() * loadKg * 100) / 100

    fun snapshot(): PrimaryViolationTier {
        return PrimaryViolationTier(
            code = code,
            label = label,
            segment = segment,
            loadKg = loadKg,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary violation tier in status requested. */
fun newPrimaryViolationTier(code: String, label: String): PrimaryViolationTier {
    return PrimaryViolationTier(
        code = code,
        label = label,
        segment = "",
        loadKg = 0.0,
        graceDays = 0,
        status = PrimaryViolationTierStatus.REQUESTED,
    )
}
