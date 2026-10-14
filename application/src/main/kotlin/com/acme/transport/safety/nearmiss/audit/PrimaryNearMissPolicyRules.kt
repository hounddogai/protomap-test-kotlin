package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.text.normalize

/** A rule a primary near miss policy must pass. Check returns its problems, or none. */
interface PrimaryNearMissPolicyRule {
    fun check(item: PrimaryNearMissPolicy): MutableList<String>
}

data class PrimaryNearMissPolicyCurrencyLengthRule(var maxLength: Int) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissPolicyCeilingRangeRule(var min: Double, var max: Double) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class PrimaryNearMissPolicyStartsAtRangeRule(var min: Int, var max: Int) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissPolicyRequiredCodeRule(var message: String) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissPolicyAllowedCodeRule(var allowed: MutableList<String>) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissPolicyMaximumRangeRule(var min: Double, var max: Double) : PrimaryNearMissPolicyRule {
    override fun check(item: PrimaryNearMissPolicy): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary near miss policy. */
data class PrimaryNearMissPolicyValidator(var rules: MutableList<PrimaryNearMissPolicyRule>) {
    fun validate(item: PrimaryNearMissPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryNearMissPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNearMissPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNearMissPolicyValidator(): PrimaryNearMissPolicyValidator {
    return PrimaryNearMissPolicyValidator(
        mutableListOf<PrimaryNearMissPolicyRule>(
            PrimaryNearMissPolicyCurrencyLengthRule(64),
            PrimaryNearMissPolicyCeilingRangeRule(6.1, 541.3),
            PrimaryNearMissPolicyStartsAtRangeRule(16, 120),
            PrimaryNearMissPolicyRequiredCodeRule("primary near miss policy needs this field"),
            PrimaryNearMissPolicyAllowedCodeRule(mutableListOf<String>()),
            PrimaryNearMissPolicyMaximumRangeRule(4.2, 2742.5),
        ),
    )
}
