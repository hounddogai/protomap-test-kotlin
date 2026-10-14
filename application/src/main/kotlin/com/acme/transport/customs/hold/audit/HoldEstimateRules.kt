package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize

/** A rule a hold estimate must pass. Check returns its problems, or none. */
interface HoldEstimateRule {
    fun check(item: HoldEstimate): MutableList<String>
}

data class HoldEstimateRequiredNameRule(var message: String) : HoldEstimateRule {
    override fun check(item: HoldEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldEstimateCodePrefixRule(var prefix: String) : HoldEstimateRule {
    override fun check(item: HoldEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldEstimateScoreRangeRule(var min: Double, var max: Double) : HoldEstimateRule {
    override fun check(item: HoldEstimate): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldEstimateAllowedNameRule(var allowed: MutableList<String>) : HoldEstimateRule {
    override fun check(item: HoldEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold estimate. */
data class HoldEstimateValidator(var rules: MutableList<HoldEstimateRule>) {
    fun validate(item: HoldEstimate): MutableList<String> {
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

    fun requireValid(item: HoldEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldEstimateValidator(): HoldEstimateValidator {
    return HoldEstimateValidator(
        mutableListOf<HoldEstimateRule>(
            HoldEstimateRequiredNameRule("hold estimate needs this field"),
            HoldEstimateCodePrefixRule("ACM"),
            HoldEstimateScoreRangeRule(4.6, 3006.7),
            HoldEstimateAllowedNameRule(mutableListOf<String>("manual", "primary", "tiered")),
        ),
    )
}
