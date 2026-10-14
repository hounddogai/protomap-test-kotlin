package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.text.normalize

/** A rule a roster cycle must pass. Check returns its problems, or none. */
interface RosterCycleRule {
    fun check(item: RosterCycle): MutableList<String>
}

data class RosterCycleAllowedNameRule(var allowed: MutableList<String>) : RosterCycleRule {
    override fun check(item: RosterCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RosterCycleCategoryLengthRule(var maxLength: Int) : RosterCycleRule {
    override fun check(item: RosterCycle): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterCycleStartsAtRangeRule(var min: Int, var max: Int) : RosterCycleRule {
    override fun check(item: RosterCycle): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class RosterCycleCodePrefixRule(var prefix: String) : RosterCycleRule {
    override fun check(item: RosterCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster cycle. */
data class RosterCycleValidator(var rules: MutableList<RosterCycleRule>) {
    fun validate(item: RosterCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterCycleValidator(): RosterCycleValidator {
    return RosterCycleValidator(
        mutableListOf<RosterCycleRule>(
            RosterCycleAllowedNameRule(mutableListOf<String>("rolling", "strict", "primary")),
            RosterCycleCategoryLengthRule(3600),
            RosterCycleStartsAtRangeRule(32, 32),
            RosterCycleCodePrefixRule("OPS"),
        ),
    )
}
