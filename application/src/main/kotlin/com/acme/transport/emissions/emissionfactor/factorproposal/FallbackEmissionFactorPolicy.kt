package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback emission factor policy is in its life cycle. */
enum class FallbackEmissionFactorPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback emission factor policy, with what the planners need to act on it. */
data class FallbackEmissionFactorPolicy(
    var code: String,
    var label: String,
    var quantity: Int,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var region: String,
    var summary: String,
    var pinned: Boolean,
    var amount: Double,
    var status: FallbackEmissionFactorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorPolicyStatus): Boolean {
        when (status) {
            FallbackEmissionFactorPolicyStatus.REQUESTED -> {
                when (next) {
                    FallbackEmissionFactorPolicyStatus.GRANTED -> return true
                }
                return false
            }
            FallbackEmissionFactorPolicyStatus.GRANTED -> {
                when (next) {
                    FallbackEmissionFactorPolicyStatus.REVOKED, FallbackEmissionFactorPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackEmissionFactorPolicyStatus.REVOKED -> {
                when (next) {
                    FallbackEmissionFactorPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"fallback emission factor policy"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((createdAt.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): FallbackEmissionFactorPolicy {
        return FallbackEmissionFactorPolicy(
            code = code,
            label = label,
            quantity = quantity,
            createdAt = createdAt,
            attributes = attributes,
            version = version,
            region = region,
            summary = summary,
            pinned = pinned,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback emission factor policy in status requested. */
fun newFallbackEmissionFactorPolicy(code: String, label: String): FallbackEmissionFactorPolicy {
    return FallbackEmissionFactorPolicy(
        code = code,
        label = label,
        quantity = 0,
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        version = 0,
        region = "",
        summary = "",
        pinned = false,
        amount = 0.0,
        status = FallbackEmissionFactorPolicyStatus.REQUESTED,
    )
}
