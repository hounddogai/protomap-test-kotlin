package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.normalize

/** A rule a rolling accessorial estimate must pass. Check returns its problems, or none. */
interface RollingAccessorialEstimateRule {
    fun check(item: RollingAccessorialEstimate): MutableList<String>
}

data class RollingAccessorialEstimateCodePrefixRule(var prefix: String) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialEstimateRequiredTitleRule(var message: String) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialEstimateStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialEstimateAllowedTitleRule(
    var allowed: MutableList<String>,
) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialEstimateTitleLengthRule(var maxLength: Int) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialEstimateQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RollingAccessorialEstimateRule {
    override fun check(item: RollingAccessorialEstimate): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling accessorial estimate. */
data class RollingAccessorialEstimateValidator(var rules: MutableList<RollingAccessorialEstimateRule>) {
    fun validate(item: RollingAccessorialEstimate): MutableList<String> {
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

    fun requireValid(item: RollingAccessorialEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAccessorialEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAccessorialEstimateValidator(): RollingAccessorialEstimateValidator {
    return RollingAccessorialEstimateValidator(
        mutableListOf<RollingAccessorialEstimateRule>(
            RollingAccessorialEstimateCodePrefixRule("X"),
            RollingAccessorialEstimateRequiredTitleRule("rolling accessorial estimate needs an entry"),
            RollingAccessorialEstimateStartsAtRangeRule(1000, 3600),
            RollingAccessorialEstimateAllowedTitleRule(mutableListOf<String>("adaptive", "global")),
            RollingAccessorialEstimateTitleLengthRule(255),
            RollingAccessorialEstimateQuantityRangeRule(255, 3600),
        ),
    )
}
