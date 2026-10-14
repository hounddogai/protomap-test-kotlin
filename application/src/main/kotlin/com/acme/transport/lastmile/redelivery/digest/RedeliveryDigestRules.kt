package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.kit.text.normalize

/** A rule a redelivery digest must pass. Check returns its problems, or none. */
interface RedeliveryDigestRule {
    fun check(item: RedeliveryDigest): MutableList<String>
}

data class RedeliveryDigestRequiredLocaleRule(var message: String) : RedeliveryDigestRule {
    override fun check(item: RedeliveryDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDigestAllowedTitleRule(var allowed: MutableList<String>) : RedeliveryDigestRule {
    override fun check(item: RedeliveryDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDigestBudgetRangeRule(var min: Double, var max: Double) : RedeliveryDigestRule {
    override fun check(item: RedeliveryDigest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RedeliveryDigestDistanceKmRangeRule(var min: Double, var max: Double) : RedeliveryDigestRule {
    override fun check(item: RedeliveryDigest): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryDigestRequiredCodeRule(var message: String) : RedeliveryDigestRule {
    override fun check(item: RedeliveryDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery digest. */
data class RedeliveryDigestValidator(var rules: MutableList<RedeliveryDigestRule>) {
    fun validate(item: RedeliveryDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryDigestValidator(): RedeliveryDigestValidator {
    return RedeliveryDigestValidator(
        mutableListOf<RedeliveryDigestRule>(
            RedeliveryDigestRequiredLocaleRule("redelivery digest needs this field"),
            RedeliveryDigestAllowedTitleRule(mutableListOf<String>("primary", "pending")),
            RedeliveryDigestBudgetRangeRule(9.3, 4742.4),
            RedeliveryDigestDistanceKmRangeRule(1.3, 78.7),
            RedeliveryDigestRequiredCodeRule("redelivery digest needs this field"),
        ),
    )
}
