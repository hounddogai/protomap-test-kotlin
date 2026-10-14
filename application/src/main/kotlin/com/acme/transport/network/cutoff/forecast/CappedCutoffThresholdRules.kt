package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.text.normalize

/** A rule a capped cutoff threshold must pass. Check returns its problems, or none. */
interface CappedCutoffThresholdRule {
    fun check(item: CappedCutoffThreshold): MutableList<String>
}

data class CappedCutoffThresholdPalletsRangeRule(var min: Int, var max: Int) : CappedCutoffThresholdRule {
    override fun check(item: CappedCutoffThreshold): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffThresholdRequiredCodeRule(var message: String) : CappedCutoffThresholdRule {
    override fun check(item: CappedCutoffThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffThresholdCodeLengthRule(var maxLength: Int) : CappedCutoffThresholdRule {
    override fun check(item: CappedCutoffThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffThresholdAllowedLabelRule(var allowed: MutableList<String>) : CappedCutoffThresholdRule {
    override fun check(item: CappedCutoffThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cutoff threshold. */
data class CappedCutoffThresholdValidator(var rules: MutableList<CappedCutoffThresholdRule>) {
    fun validate(item: CappedCutoffThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCutoffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCutoffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCutoffThresholdValidator(): CappedCutoffThresholdValidator {
    return CappedCutoffThresholdValidator(
        mutableListOf<CappedCutoffThresholdRule>(
            CappedCutoffThresholdPalletsRangeRule(80, 80),
            CappedCutoffThresholdRequiredCodeRule("capped cutoff threshold needs a value"),
            CappedCutoffThresholdCodeLengthRule(64),
            CappedCutoffThresholdAllowedLabelRule(mutableListOf<String>("fallback", "tiered", "pending")),
        ),
    )
}
