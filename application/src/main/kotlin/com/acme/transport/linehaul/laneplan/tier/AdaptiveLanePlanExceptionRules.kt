package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.text.normalize

/** A rule a adaptive lane plan exception must pass. Check returns its problems, or none. */
interface AdaptiveLanePlanExceptionRule {
    fun check(item: AdaptiveLanePlanException): MutableList<String>
}

data class AdaptiveLanePlanExceptionCodePrefixRule(var prefix: String) : AdaptiveLanePlanExceptionRule {
    override fun check(item: AdaptiveLanePlanException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanExceptionAllowedTitleRule(
    var allowed: MutableList<String>,
) : AdaptiveLanePlanExceptionRule {
    override fun check(item: AdaptiveLanePlanException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLanePlanExceptionRateRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveLanePlanExceptionRule {
    override fun check(item: AdaptiveLanePlanException): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lane plan exception. */
data class AdaptiveLanePlanExceptionValidator(var rules: MutableList<AdaptiveLanePlanExceptionRule>) {
    fun validate(item: AdaptiveLanePlanException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLanePlanException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLanePlanException>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLanePlanExceptionValidator(): AdaptiveLanePlanExceptionValidator {
    return AdaptiveLanePlanExceptionValidator(
        mutableListOf<AdaptiveLanePlanExceptionRule>(
            AdaptiveLanePlanExceptionCodePrefixRule("OPS"),
            AdaptiveLanePlanExceptionAllowedTitleRule(mutableListOf<String>("default")),
            AdaptiveLanePlanExceptionRateRangeRule(1.9, 4945.3),
        ),
    )
}
