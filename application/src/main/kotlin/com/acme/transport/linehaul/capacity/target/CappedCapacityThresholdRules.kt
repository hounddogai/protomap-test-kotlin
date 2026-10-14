package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize

/** A rule a capped capacity threshold must pass. Check returns its problems, or none. */
interface CappedCapacityThresholdRule {
    fun check(item: CappedCapacityThreshold): MutableList<String>
}

data class CappedCapacityThresholdCodeLengthRule(var maxLength: Int) : CappedCapacityThresholdRule {
    override fun check(item: CappedCapacityThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityThresholdAllowedPhoneRule(
    var allowed: MutableList<String>,
) : CappedCapacityThresholdRule {
    override fun check(item: CappedCapacityThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityThresholdAttemptsRangeRule(var min: Int, var max: Int) : CappedCapacityThresholdRule {
    override fun check(item: CappedCapacityThreshold): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped capacity threshold. */
data class CappedCapacityThresholdValidator(var rules: MutableList<CappedCapacityThresholdRule>) {
    fun validate(item: CappedCapacityThreshold): MutableList<String> {
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

    fun requireValid(item: CappedCapacityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCapacityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCapacityThresholdValidator(): CappedCapacityThresholdValidator {
    return CappedCapacityThresholdValidator(
        mutableListOf<CappedCapacityThresholdRule>(
            CappedCapacityThresholdCodeLengthRule(120),
            CappedCapacityThresholdAllowedPhoneRule(mutableListOf<String>("rolling")),
            CappedCapacityThresholdAttemptsRangeRule(80, 1000),
        ),
    )
}
