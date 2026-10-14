package com.acme.transport.fleetops.utilization.utilizationoverride

/** A rule a regional utilization override must pass. Check returns its problems, or none. */
interface RegionalUtilizationOverrideRule {
    fun check(item: RegionalUtilizationOverride): MutableList<String>
}

data class RegionalUtilizationOverrideBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationOverrideRequiredCodeRule(var message: String) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationOverrideSourceLengthRule(var maxLength: Int) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationOverridePriorityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationOverrideTitleLengthRule(var maxLength: Int) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationOverrideRequiredSourceRule(
    var message: String,
) : RegionalUtilizationOverrideRule {
    override fun check(item: RegionalUtilizationOverride): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional utilization override. */
data class RegionalUtilizationOverrideValidator(var rules: MutableList<RegionalUtilizationOverrideRule>) {
    fun validate(item: RegionalUtilizationOverride): MutableList<String> {
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

    fun requireValid(item: RegionalUtilizationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalUtilizationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalUtilizationOverrideValidator(): RegionalUtilizationOverrideValidator {
    return RegionalUtilizationOverrideValidator(
        mutableListOf<RegionalUtilizationOverrideRule>(
            RegionalUtilizationOverrideBudgetRangeRule(1.9, 1912.7),
            RegionalUtilizationOverrideRequiredCodeRule("regional utilization override needs this field"),
            RegionalUtilizationOverrideSourceLengthRule(32),
            RegionalUtilizationOverridePriorityRangeRule(64, 1000),
            RegionalUtilizationOverrideTitleLengthRule(64),
            RegionalUtilizationOverrideRequiredSourceRule("regional utilization override needs an entry"),
        ),
    )
}
