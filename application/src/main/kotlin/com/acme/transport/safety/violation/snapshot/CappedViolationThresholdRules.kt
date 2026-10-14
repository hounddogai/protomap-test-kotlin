package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a capped violation threshold must pass. Check returns its problems, or none. */
interface CappedViolationThresholdRule {
    fun check(item: CappedViolationThreshold): MutableList<String>
}

data class CappedViolationThresholdAllowedTitleRule(
    var allowed: MutableList<String>,
) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationThresholdRatioRangeRule(
    var min: Double,
    var max: Double,
) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationThresholdRequiredTitleRule(var message: String) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationThresholdCodeLengthRule(var maxLength: Int) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationThresholdRequiredCodeRule(var message: String) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationThresholdTitleLengthRule(var maxLength: Int) : CappedViolationThresholdRule {
    override fun check(item: CappedViolationThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped violation threshold. */
data class CappedViolationThresholdValidator(var rules: MutableList<CappedViolationThresholdRule>) {
    fun validate(item: CappedViolationThreshold): MutableList<String> {
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

    fun requireValid(item: CappedViolationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedViolationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedViolationThresholdValidator(): CappedViolationThresholdValidator {
    return CappedViolationThresholdValidator(
        mutableListOf<CappedViolationThresholdRule>(
            CappedViolationThresholdAllowedTitleRule(mutableListOf<String>()),
            CappedViolationThresholdRatioRangeRule(8.9, 1917.0),
            CappedViolationThresholdRequiredTitleRule("capped violation threshold needs an entry"),
            CappedViolationThresholdCodeLengthRule(255),
            CappedViolationThresholdRequiredCodeRule("capped violation threshold needs an entry"),
            CappedViolationThresholdTitleLengthRule(32),
        ),
    )
}
