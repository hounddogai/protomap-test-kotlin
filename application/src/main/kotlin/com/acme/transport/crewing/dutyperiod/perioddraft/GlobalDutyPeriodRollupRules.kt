package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize

/** A rule a global duty period rollup must pass. Check returns its problems, or none. */
interface GlobalDutyPeriodRollupRule {
    fun check(item: GlobalDutyPeriodRollup): MutableList<String>
}

data class GlobalDutyPeriodRollupAllowedUnitRule(
    var allowed: MutableList<String>,
) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodRollupWeightRangeRule(
    var min: Double,
    var max: Double,
) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodRollupLabelLengthRule(var maxLength: Int) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodRollupRequiredCodeRule(var message: String) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodRollupUnitLengthRule(var maxLength: Int) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDutyPeriodRollupCodePrefixRule(var prefix: String) : GlobalDutyPeriodRollupRule {
    override fun check(item: GlobalDutyPeriodRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global duty period rollup. */
data class GlobalDutyPeriodRollupValidator(var rules: MutableList<GlobalDutyPeriodRollupRule>) {
    fun validate(item: GlobalDutyPeriodRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDutyPeriodRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDutyPeriodRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDutyPeriodRollupValidator(): GlobalDutyPeriodRollupValidator {
    return GlobalDutyPeriodRollupValidator(
        mutableListOf<GlobalDutyPeriodRollupRule>(
            GlobalDutyPeriodRollupAllowedUnitRule(mutableListOf<String>()),
            GlobalDutyPeriodRollupWeightRangeRule(7.7, 2048.2),
            GlobalDutyPeriodRollupLabelLengthRule(3600),
            GlobalDutyPeriodRollupRequiredCodeRule("global duty period rollup needs a value"),
            GlobalDutyPeriodRollupUnitLengthRule(16),
            GlobalDutyPeriodRollupCodePrefixRule("OPS"),
        ),
    )
}
