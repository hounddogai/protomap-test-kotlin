package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.text.normalize

/** A rule a manual ev charge score must pass. Check returns its problems, or none. */
interface ManualEvChargeScoreRule {
    fun check(item: ManualEvChargeScore): MutableList<String>
}

data class ManualEvChargeScoreMaximumRangeRule(var min: Double, var max: Double) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ManualEvChargeScoreCodePrefixRule(var prefix: String) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeScoreEndsAtRangeRule(var min: Int, var max: Int) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeScoreRequiredTitleRule(var message: String) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeScoreRequiredTrailerIdRule(var message: String) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeScoreAllowedCodeRule(var allowed: MutableList<String>) : ManualEvChargeScoreRule {
    override fun check(item: ManualEvChargeScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual ev charge score. */
data class ManualEvChargeScoreValidator(var rules: MutableList<ManualEvChargeScoreRule>) {
    fun validate(item: ManualEvChargeScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualEvChargeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualEvChargeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultManualEvChargeScoreValidator(): ManualEvChargeScoreValidator {
    return ManualEvChargeScoreValidator(
        mutableListOf<ManualEvChargeScoreRule>(
            ManualEvChargeScoreMaximumRangeRule(0.4, 3918.0),
            ManualEvChargeScoreCodePrefixRule("ACM"),
            ManualEvChargeScoreEndsAtRangeRule(16, 255),
            ManualEvChargeScoreRequiredTitleRule("manual ev charge score needs this field"),
            ManualEvChargeScoreRequiredTrailerIdRule("manual ev charge score needs this field"),
            ManualEvChargeScoreAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
