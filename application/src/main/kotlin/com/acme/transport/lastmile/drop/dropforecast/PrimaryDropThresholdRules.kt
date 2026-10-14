package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize

/** A rule a primary drop threshold must pass. Check returns its problems, or none. */
interface PrimaryDropThresholdRule {
    fun check(item: PrimaryDropThreshold): MutableList<String>
}

data class PrimaryDropThresholdMarginRangeRule(var min: Double, var max: Double) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
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

data class PrimaryDropThresholdCodePrefixRule(var prefix: String) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropThresholdCurrencyLengthRule(var maxLength: Int) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropThresholdRequiredNameRule(var message: String) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropThresholdAllowedNameRule(var allowed: MutableList<String>) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropThresholdRequiredCurrencyRule(var message: String) : PrimaryDropThresholdRule {
    override fun check(item: PrimaryDropThreshold): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary drop threshold. */
data class PrimaryDropThresholdValidator(var rules: MutableList<PrimaryDropThresholdRule>) {
    fun validate(item: PrimaryDropThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDropThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDropThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDropThresholdValidator(): PrimaryDropThresholdValidator {
    return PrimaryDropThresholdValidator(
        mutableListOf<PrimaryDropThresholdRule>(
            PrimaryDropThresholdMarginRangeRule(6.6, 2730.4),
            PrimaryDropThresholdCodePrefixRule("REF"),
            PrimaryDropThresholdCurrencyLengthRule(80),
            PrimaryDropThresholdRequiredNameRule("primary drop threshold needs a value"),
            PrimaryDropThresholdAllowedNameRule(mutableListOf<String>()),
            PrimaryDropThresholdRequiredCurrencyRule("primary drop threshold needs a value"),
        ),
    )
}
