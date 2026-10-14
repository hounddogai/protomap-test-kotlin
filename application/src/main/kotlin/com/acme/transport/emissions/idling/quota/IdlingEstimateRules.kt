package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.text.normalize

/** A rule a idling estimate must pass. Check returns its problems, or none. */
interface IdlingEstimateRule {
    fun check(item: IdlingEstimate): MutableList<String>
}

data class IdlingEstimateAllowedUnitRule(var allowed: MutableList<String>) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class IdlingEstimateCodePrefixRule(var prefix: String) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingEstimateNameLengthRule(var maxLength: Int) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingEstimateCodeLengthRule(var maxLength: Int) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingEstimateCostRangeRule(var min: Double, var max: Double) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
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

data class IdlingEstimateRequiredUnitRule(var message: String) : IdlingEstimateRule {
    override fun check(item: IdlingEstimate): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling estimate. */
data class IdlingEstimateValidator(var rules: MutableList<IdlingEstimateRule>) {
    fun validate(item: IdlingEstimate): MutableList<String> {
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

    fun requireValid(item: IdlingEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingEstimateValidator(): IdlingEstimateValidator {
    return IdlingEstimateValidator(
        mutableListOf<IdlingEstimateRule>(
            IdlingEstimateAllowedUnitRule(mutableListOf<String>("primary", "adaptive", "manual")),
            IdlingEstimateCodePrefixRule("X"),
            IdlingEstimateNameLengthRule(1000),
            IdlingEstimateCodeLengthRule(64),
            IdlingEstimateCostRangeRule(6.0, 2073.8),
            IdlingEstimateRequiredUnitRule("idling estimate needs this field"),
        ),
    )
}
