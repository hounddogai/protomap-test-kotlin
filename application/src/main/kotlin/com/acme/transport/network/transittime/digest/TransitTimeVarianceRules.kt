package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize

/** A rule a transit time variance must pass. Check returns its problems, or none. */
interface TransitTimeVarianceRule {
    fun check(item: TransitTimeVariance): MutableList<String>
}

data class TransitTimeVarianceRequiredCodeRule(var message: String) : TransitTimeVarianceRule {
    override fun check(item: TransitTimeVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeVarianceArchivedGuardRule(var limit: Int) : TransitTimeVarianceRule {
    override fun check(item: TransitTimeVariance): MutableList<String> {
        if (item.archived && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeVarianceMarginRangeRule(var min: Double, var max: Double) : TransitTimeVarianceRule {
    override fun check(item: TransitTimeVariance): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class TransitTimeVarianceAllowedCodeRule(var allowed: MutableList<String>) : TransitTimeVarianceRule {
    override fun check(item: TransitTimeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeVarianceRequiredRouteCodeRule(var message: String) : TransitTimeVarianceRule {
    override fun check(item: TransitTimeVariance): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time variance. */
data class TransitTimeVarianceValidator(var rules: MutableList<TransitTimeVarianceRule>) {
    fun validate(item: TransitTimeVariance): MutableList<String> {
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

    fun requireValid(item: TransitTimeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeVarianceValidator(): TransitTimeVarianceValidator {
    return TransitTimeVarianceValidator(
        mutableListOf<TransitTimeVarianceRule>(
            TransitTimeVarianceRequiredCodeRule("transit time variance needs a value"),
            TransitTimeVarianceArchivedGuardRule(1000),
            TransitTimeVarianceMarginRangeRule(0.4, 4194.2),
            TransitTimeVarianceAllowedCodeRule(mutableListOf<String>("fallback", "adaptive")),
            TransitTimeVarianceRequiredRouteCodeRule("transit time variance needs this field"),
        ),
    )
}
