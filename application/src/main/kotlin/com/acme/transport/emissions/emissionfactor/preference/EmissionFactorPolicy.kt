package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor policy is in its life cycle. */
enum class EmissionFactorPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a emission factor policy and its current status. */
data class EmissionFactorPolicy(
    var code: String,
    var label: String,
    var ownerId: String,
    var summary: String,
    var rate: Double,
    var checksum: String,
    var limit: Int,
    var threshold: Double,
    var status: EmissionFactorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorPolicyStatus): Boolean {
        when (status) {
            EmissionFactorPolicyStatus.PROPOSED -> return next == EmissionFactorPolicyStatus.REJECTED || next == EmissionFactorPolicyStatus.WITHDRAWN
            EmissionFactorPolicyStatus.APPROVED -> return next == EmissionFactorPolicyStatus.REJECTED
            EmissionFactorPolicyStatus.REJECTED -> return next == EmissionFactorPolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: EmissionFactorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("emission factor policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"emission factor policy"} $code"

    fun weightedVolume(): Double = round((limit.toDouble() - threshold) * 100) / 100

    fun snapshot(): EmissionFactorPolicy {
        return EmissionFactorPolicy(
            code = code,
            label = label,
            ownerId = ownerId,
            summary = summary,
            rate = rate,
            checksum = checksum,
            limit = limit,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor policy in status proposed. */
fun newEmissionFactorPolicy(code: String, label: String): EmissionFactorPolicy {
    return EmissionFactorPolicy(
        code = code,
        label = label,
        ownerId = "",
        summary = "",
        rate = 0.0,
        checksum = "",
        limit = 0,
        threshold = 0.0,
        status = EmissionFactorPolicyStatus.PROPOSED,
    )
}
