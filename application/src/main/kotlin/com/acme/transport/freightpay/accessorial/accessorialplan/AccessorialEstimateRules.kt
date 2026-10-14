package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.text.normalize

/** A rule a accessorial estimate must pass. Check returns its problems, or none. */
interface AccessorialEstimateRule {
    fun check(item: AccessorialEstimate): MutableList<String>
}

data class AccessorialEstimateExpiresAtRangeRule(var min: Int, var max: Int) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class AccessorialEstimateRequiredTitleRule(var message: String) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialEstimateRequiredChannelRule(var message: String) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialEstimateAllowedDriverIdRule(var allowed: MutableList<String>) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialEstimateTitleLengthRule(var maxLength: Int) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialEstimateCostRangeRule(var min: Double, var max: Double) : AccessorialEstimateRule {
    override fun check(item: AccessorialEstimate): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a accessorial estimate. */
data class AccessorialEstimateValidator(var rules: MutableList<AccessorialEstimateRule>) {
    fun validate(item: AccessorialEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialEstimateValidator(): AccessorialEstimateValidator {
    return AccessorialEstimateValidator(
        mutableListOf<AccessorialEstimateRule>(
            AccessorialEstimateExpiresAtRangeRule(32, 255),
            AccessorialEstimateRequiredTitleRule("accessorial estimate needs this field"),
            AccessorialEstimateRequiredChannelRule("accessorial estimate needs a value"),
            AccessorialEstimateAllowedDriverIdRule(mutableListOf<String>("fallback", "lenient")),
            AccessorialEstimateTitleLengthRule(3600),
            AccessorialEstimateCostRangeRule(5.9, 2357.2),
        ),
    )
}
