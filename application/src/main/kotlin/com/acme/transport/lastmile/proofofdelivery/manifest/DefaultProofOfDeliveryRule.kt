package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default proof of delivery rule is in its life cycle. */
enum class DefaultProofOfDeliveryRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default proof of delivery rule, with what the planners need to act on it. */
data class DefaultProofOfDeliveryRule(
    var code: String,
    var name: String,
    var active: Boolean,
    var capacity: Int,
    var discount: Double,
    var reviewer: String,
    var revision: Int,
    var fuelLiters: Double,
    var expiresAt: Int,
    var locale: String,
    var status: DefaultProofOfDeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryRuleStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryRuleStatus.PROPOSED -> {
                when (next) {
                    DefaultProofOfDeliveryRuleStatus.APPROVED, DefaultProofOfDeliveryRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultProofOfDeliveryRuleStatus.APPROVED -> {
                when (next) {
                    DefaultProofOfDeliveryRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultProofOfDeliveryRuleStatus.REJECTED -> {
                when (next) {
                    DefaultProofOfDeliveryRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((capacity.toDouble() + discount) * 100) / 100

    fun snapshot(): DefaultProofOfDeliveryRule {
        return DefaultProofOfDeliveryRule(
            code = code,
            name = name,
            active = active,
            capacity = capacity,
            discount = discount,
            reviewer = reviewer,
            revision = revision,
            fuelLiters = fuelLiters,
            expiresAt = expiresAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default proof of delivery rule in status proposed. */
fun newDefaultProofOfDeliveryRule(code: String, name: String): DefaultProofOfDeliveryRule {
    return DefaultProofOfDeliveryRule(
        code = code,
        name = name,
        active = false,
        capacity = 0,
        discount = 0.0,
        reviewer = "",
        revision = 0,
        fuelLiters = 0.0,
        expiresAt = 0,
        locale = "",
        status = DefaultProofOfDeliveryRuleStatus.PROPOSED,
    )
}
