package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.text.normalize

/** A rule a utilization score must pass. Check returns its problems, or none. */
interface UtilizationScoreRule {
    fun check(item: UtilizationScore): MutableList<String>
}

data class UtilizationScoreRequiredCurrencyRule(var message: String) : UtilizationScoreRule {
    override fun check(item: UtilizationScore): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScoreCodePrefixRule(var prefix: String) : UtilizationScoreRule {
    override fun check(item: UtilizationScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScoreAllowedNameRule(var allowed: MutableList<String>) : UtilizationScoreRule {
    override fun check(item: UtilizationScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScorePalletsRangeRule(var min: Int, var max: Int) : UtilizationScoreRule {
    override fun check(item: UtilizationScore): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationScoreCurrencyLengthRule(var maxLength: Int) : UtilizationScoreRule {
    override fun check(item: UtilizationScore): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization score. */
data class UtilizationScoreValidator(var rules: MutableList<UtilizationScoreRule>) {
    fun validate(item: UtilizationScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationScoreValidator(): UtilizationScoreValidator {
    return UtilizationScoreValidator(
        mutableListOf<UtilizationScoreRule>(
            UtilizationScoreRequiredCurrencyRule("utilization score needs a value"),
            UtilizationScoreCodePrefixRule("ACM"),
            UtilizationScoreAllowedNameRule(mutableListOf<String>("seasonal")),
            UtilizationScorePalletsRangeRule(64, 64),
            UtilizationScoreCurrencyLengthRule(255),
        ),
    )
}
