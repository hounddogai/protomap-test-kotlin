package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.text.normalize

/** A rule a capped scorecard override must pass. Check returns its problems, or none. */
interface CappedScorecardOverrideRule {
    fun check(item: CappedScorecardOverride): MutableList<String>
}

data class CappedScorecardOverrideCodeLengthRule(var maxLength: Int) : CappedScorecardOverrideRule {
    override fun check(item: CappedScorecardOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardOverrideCostRangeRule(
    var min: Double,
    var max: Double,
) : CappedScorecardOverrideRule {
    override fun check(item: CappedScorecardOverride): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardOverrideLabelLengthRule(var maxLength: Int) : CappedScorecardOverrideRule {
    override fun check(item: CappedScorecardOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardOverrideCodePrefixRule(var prefix: String) : CappedScorecardOverrideRule {
    override fun check(item: CappedScorecardOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardOverrideAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : CappedScorecardOverrideRule {
    override fun check(item: CappedScorecardOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped scorecard override. */
data class CappedScorecardOverrideValidator(var rules: MutableList<CappedScorecardOverrideRule>) {
    fun validate(item: CappedScorecardOverride): MutableList<String> {
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

    fun requireValid(item: CappedScorecardOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedScorecardOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedScorecardOverrideValidator(): CappedScorecardOverrideValidator {
    return CappedScorecardOverrideValidator(
        mutableListOf<CappedScorecardOverrideRule>(
            CappedScorecardOverrideCodeLengthRule(3600),
            CappedScorecardOverrideCostRangeRule(4.3, 718.3),
            CappedScorecardOverrideLabelLengthRule(255),
            CappedScorecardOverrideCodePrefixRule("Q"),
            CappedScorecardOverrideAllowedExternalRefRule(mutableListOf<String>("primary", "primary")),
        ),
    )
}
