package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient proof of delivery rule is in its life cycle. */
enum class LenientProofOfDeliveryRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient proof of delivery rule and its current status. */
data class LenientProofOfDeliveryRule(
    var code: String,
    var label: String,
    var limit: Int,
    var tags: MutableList<String>,
    var timezone: String,
    var archived: Boolean,
    var priority: Int,
    var description: String,
    var discount: Double,
    var capacity: Int,
    var status: LenientProofOfDeliveryRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientProofOfDeliveryRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientProofOfDeliveryRuleStatus): Boolean {
        when (status) {
            LenientProofOfDeliveryRuleStatus.DRAFT -> {
                when (next) {
                    LenientProofOfDeliveryRuleStatus.PENDING, LenientProofOfDeliveryRuleStatus.CLOSED -> return true
                }
                return false
            }
            LenientProofOfDeliveryRuleStatus.PENDING -> return next == LenientProofOfDeliveryRuleStatus.ACTIVE || next == LenientProofOfDeliveryRuleStatus.SUSPENDED
            LenientProofOfDeliveryRuleStatus.ACTIVE -> {
                when (next) {
                    LenientProofOfDeliveryRuleStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientProofOfDeliveryRuleStatus.SUSPENDED -> {
                when (next) {
                    LenientProofOfDeliveryRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientProofOfDeliveryRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient proof of delivery rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(discount * limit.toDouble() * 100) / 100

    fun snapshot(): LenientProofOfDeliveryRule {
        return LenientProofOfDeliveryRule(
            code = code,
            label = label,
            limit = limit,
            tags = tags,
            timezone = timezone,
            archived = archived,
            priority = priority,
            description = description,
            discount = discount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient proof of delivery rule in status draft. */
fun newLenientProofOfDeliveryRule(code: String, label: String): LenientProofOfDeliveryRule {
    return LenientProofOfDeliveryRule(
        code = code,
        label = label,
        limit = 0,
        tags = mutableListOf<String>(),
        timezone = "",
        archived = false,
        priority = 0,
        description = "",
        discount = 0.0,
        capacity = 0,
        status = LenientProofOfDeliveryRuleStatus.DRAFT,
    )
}
