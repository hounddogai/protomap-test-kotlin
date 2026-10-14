package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery rule is in its life cycle. */
enum class ProofOfDeliveryRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One proof of delivery rule, with what the planners need to act on it. */
data class ProofOfDeliveryRule(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var ceiling: Double,
    var quantity: Int,
    var durationMinutes: Int,
    var createdAt: Int,
    var status: ProofOfDeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryRuleStatus): Boolean {
        when (status) {
            ProofOfDeliveryRuleStatus.REQUESTED -> {
                when (next) {
                    ProofOfDeliveryRuleStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryRuleStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryRuleStatus.REVOKED, ProofOfDeliveryRuleStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryRuleStatus.REVOKED -> return next == ProofOfDeliveryRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((durationMinutes.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryRule {
        return ProofOfDeliveryRule(
            code = code,
            label = label,
            attributes = attributes,
            cost = cost,
            ceiling = ceiling,
            quantity = quantity,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery rule in status requested. */
fun newProofOfDeliveryRule(code: String, label: String): ProofOfDeliveryRule {
    return ProofOfDeliveryRule(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        ceiling = 0.0,
        quantity = 0,
        durationMinutes = 0,
        createdAt = 0,
        status = ProofOfDeliveryRuleStatus.REQUESTED,
    )
}
