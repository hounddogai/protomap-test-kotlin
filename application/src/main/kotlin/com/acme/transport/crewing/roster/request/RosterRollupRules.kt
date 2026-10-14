package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.text.normalize

/** A rule a roster rollup must pass. Check returns its problems, or none. */
interface RosterRollupRule {
    fun check(item: RosterRollup): MutableList<String>
}

data class RosterRollupAllowedLabelRule(var allowed: MutableList<String>) : RosterRollupRule {
    override fun check(item: RosterRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RosterRollupCodeLengthRule(var maxLength: Int) : RosterRollupRule {
    override fun check(item: RosterRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRollupRegionLengthRule(var maxLength: Int) : RosterRollupRule {
    override fun check(item: RosterRollup): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRollupWeightRangeRule(var min: Double, var max: Double) : RosterRollupRule {
    override fun check(item: RosterRollup): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a roster rollup. */
data class RosterRollupValidator(var rules: MutableList<RosterRollupRule>) {
    fun validate(item: RosterRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterRollupValidator(): RosterRollupValidator {
    return RosterRollupValidator(
        mutableListOf<RosterRollupRule>(
            RosterRollupAllowedLabelRule(mutableListOf<String>("manual", "primary", "weighted")),
            RosterRollupCodeLengthRule(120),
            RosterRollupRegionLengthRule(16),
            RosterRollupWeightRangeRule(2.2, 3622.0),
        ),
    )
}
