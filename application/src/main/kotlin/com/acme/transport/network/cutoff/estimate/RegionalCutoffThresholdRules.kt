package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize

/** A rule a regional cutoff threshold must pass. Check returns its problems, or none. */
interface RegionalCutoffThresholdRule {
    fun check(item: RegionalCutoffThreshold): MutableList<String>
}

data class RegionalCutoffThresholdRequiredCodeRule(var message: String) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffThresholdCodeLengthRule(var maxLength: Int) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffThresholdLabelLengthRule(var maxLength: Int) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffThresholdEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffThresholdRequiredTitleRule(var message: String) : RegionalCutoffThresholdRule {
    override fun check(item: RegionalCutoffThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cutoff threshold. */
data class RegionalCutoffThresholdValidator(var rules: MutableList<RegionalCutoffThresholdRule>) {
    fun validate(item: RegionalCutoffThreshold): MutableList<String> {
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

    fun requireValid(item: RegionalCutoffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffThresholdValidator(): RegionalCutoffThresholdValidator {
    return RegionalCutoffThresholdValidator(
        mutableListOf<RegionalCutoffThresholdRule>(
            RegionalCutoffThresholdRequiredCodeRule("regional cutoff threshold needs an entry"),
            RegionalCutoffThresholdAllowedCodeRule(mutableListOf<String>("pending", "rolling", "lenient")),
            RegionalCutoffThresholdCodeLengthRule(86400),
            RegionalCutoffThresholdLabelLengthRule(64),
            RegionalCutoffThresholdEtaMinutesRangeRule(120, 3600),
            RegionalCutoffThresholdRequiredTitleRule("regional cutoff threshold needs this field"),
        ),
    )
}
