package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize

/** A rule a rolling roster cycle must pass. Check returns its problems, or none. */
interface RollingRosterCycleRule {
    fun check(item: RollingRosterCycle): MutableList<String>
}

data class RollingRosterCycleCodeLengthRule(var maxLength: Int) : RollingRosterCycleRule {
    override fun check(item: RollingRosterCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterCycleAllowedChannelRule(var allowed: MutableList<String>) : RollingRosterCycleRule {
    override fun check(item: RollingRosterCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterCycleBudgetRangeRule(var min: Double, var max: Double) : RollingRosterCycleRule {
    override fun check(item: RollingRosterCycle): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class RollingRosterCycleRequiredNameRule(var message: String) : RollingRosterCycleRule {
    override fun check(item: RollingRosterCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling roster cycle. */
data class RollingRosterCycleValidator(var rules: MutableList<RollingRosterCycleRule>) {
    fun validate(item: RollingRosterCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRosterCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRosterCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRosterCycleValidator(): RollingRosterCycleValidator {
    return RollingRosterCycleValidator(
        mutableListOf<RollingRosterCycleRule>(
            RollingRosterCycleCodeLengthRule(16),
            RollingRosterCycleAllowedChannelRule(mutableListOf<String>("tiered", "fallback")),
            RollingRosterCycleBudgetRangeRule(4.0, 3791.9),
            RollingRosterCycleRequiredNameRule("rolling roster cycle needs this field"),
        ),
    )
}
