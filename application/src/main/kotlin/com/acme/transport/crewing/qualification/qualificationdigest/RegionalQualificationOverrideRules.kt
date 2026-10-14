package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize

/** A rule a regional qualification override must pass. Check returns its problems, or none. */
interface RegionalQualificationOverrideRule {
    fun check(item: RegionalQualificationOverride): MutableList<String>
}

data class RegionalQualificationOverrideCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalQualificationOverrideRule {
    override fun check(item: RegionalQualificationOverride): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationOverrideCodePrefixRule(var prefix: String) : RegionalQualificationOverrideRule {
    override fun check(item: RegionalQualificationOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationOverrideRequiredLabelRule(
    var message: String,
) : RegionalQualificationOverrideRule {
    override fun check(item: RegionalQualificationOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalQualificationOverrideRule {
    override fun check(item: RegionalQualificationOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationOverrideCodeLengthRule(var maxLength: Int) : RegionalQualificationOverrideRule {
    override fun check(item: RegionalQualificationOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification override. */
data class RegionalQualificationOverrideValidator(var rules: MutableList<RegionalQualificationOverrideRule>) {
    fun validate(item: RegionalQualificationOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalQualificationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationOverrideValidator(): RegionalQualificationOverrideValidator {
    return RegionalQualificationOverrideValidator(
        mutableListOf<RegionalQualificationOverrideRule>(
            RegionalQualificationOverrideCostRangeRule(8.0, 2676.6),
            RegionalQualificationOverrideCodePrefixRule("Q"),
            RegionalQualificationOverrideRequiredLabelRule("regional qualification override needs an entry"),
            RegionalQualificationOverrideAllowedCodeRule(
                mutableListOf<String>("batched", "scheduled", "scheduled"),
            ),
            RegionalQualificationOverrideCodeLengthRule(16),
        ),
    )
}
