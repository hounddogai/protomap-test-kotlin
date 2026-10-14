package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a regional lane plan override must pass. Check returns its problems, or none. */
interface RegionalLanePlanOverrideRule {
    fun check(item: RegionalLanePlanOverride): MutableList<String>
}

data class RegionalLanePlanOverrideCodePrefixRule(var prefix: String) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanOverrideRequiredTimezoneRule(var message: String) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanOverrideRequiredCodeRule(var message: String) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanOverrideTimezoneLengthRule(var maxLength: Int) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanOverrideAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLanePlanOverrideBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : RegionalLanePlanOverrideRule {
    override fun check(item: RegionalLanePlanOverride): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional lane plan override. */
data class RegionalLanePlanOverrideValidator(var rules: MutableList<RegionalLanePlanOverrideRule>) {
    fun validate(item: RegionalLanePlanOverride): MutableList<String> {
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

    fun requireValid(item: RegionalLanePlanOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLanePlanOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLanePlanOverrideValidator(): RegionalLanePlanOverrideValidator {
    return RegionalLanePlanOverrideValidator(
        mutableListOf<RegionalLanePlanOverrideRule>(
            RegionalLanePlanOverrideCodePrefixRule("OPS"),
            RegionalLanePlanOverrideRequiredTimezoneRule("regional lane plan override needs this field"),
            RegionalLanePlanOverrideRequiredCodeRule("regional lane plan override needs a value"),
            RegionalLanePlanOverrideTimezoneLengthRule(64),
            RegionalLanePlanOverrideAllowedDescriptionRule(mutableListOf<String>()),
            RegionalLanePlanOverrideBatchSizeRangeRule(1000, 3600),
        ),
    )
}
