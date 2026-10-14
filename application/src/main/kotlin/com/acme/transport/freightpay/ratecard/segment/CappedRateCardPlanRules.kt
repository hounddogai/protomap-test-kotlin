package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize

/** A rule a capped rate card plan must pass. Check returns its problems, or none. */
interface CappedRateCardPlanRule {
    fun check(item: CappedRateCardPlan): MutableList<String>
}

data class CappedRateCardPlanRequiredCodeRule(var message: String) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardPlanAllowedRouteCodeRule(var allowed: MutableList<String>) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardPlanNameLengthRule(var maxLength: Int) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardPlanRouteCodeLengthRule(var maxLength: Int) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardPlanRequiredRouteCodeRule(var message: String) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardPlanCodePrefixRule(var prefix: String) : CappedRateCardPlanRule {
    override fun check(item: CappedRateCardPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rate card plan. */
data class CappedRateCardPlanValidator(var rules: MutableList<CappedRateCardPlanRule>) {
    fun validate(item: CappedRateCardPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: CappedRateCardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardPlan>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultCappedRateCardPlanValidator(): CappedRateCardPlanValidator {
    return CappedRateCardPlanValidator(
        mutableListOf<CappedRateCardPlanRule>(
            CappedRateCardPlanRequiredCodeRule("capped rate card plan needs this field"),
            CappedRateCardPlanAllowedRouteCodeRule(mutableListOf<String>("legacy", "pending")),
            CappedRateCardPlanNameLengthRule(16),
            CappedRateCardPlanRouteCodeLengthRule(3600),
            CappedRateCardPlanRequiredRouteCodeRule("capped rate card plan needs this field"),
            CappedRateCardPlanCodePrefixRule("OPS"),
        ),
    )
}
