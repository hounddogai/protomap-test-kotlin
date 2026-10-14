package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.normalize

// The near miss rule records of safety's near miss work, and the rules, stores, and calculations around them.

/** Where a near miss rule is in its life cycle. */
enum class NearMissRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A near miss rule. Its code is unique within a tenant. */
data class NearMissRule(
    var code: String,
    var title: String,
    var phone: String,
    var currency: String,
    var locale: String,
    var budget: Double,
    var status: NearMissRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissRuleStatus): Boolean {
        when (status) {
            NearMissRuleStatus.CREATED -> return next == NearMissRuleStatus.PUBLISHED
            NearMissRuleStatus.VALIDATED -> {
                when (next) {
                    NearMissRuleStatus.PUBLISHED, NearMissRuleStatus.RETIRED -> return true
                }
                return false
            }
            NearMissRuleStatus.PUBLISHED -> {
                when (next) {
                    NearMissRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissRuleStatus) {
        if (!canMoveTo(next)) {
            error("near miss rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = budget

    fun snapshot(): NearMissRule {
        return NearMissRule(
            code = code,
            title = title,
            phone = phone,
            currency = currency,
            locale = locale,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a near miss rule in status created. */
fun newNearMissRule(code: String, title: String): NearMissRule {
    return NearMissRule(
        code = code,
        title = title,
        phone = "",
        currency = "",
        locale = "",
        budget = 0.0,
        status = NearMissRuleStatus.CREATED,
    )
}
