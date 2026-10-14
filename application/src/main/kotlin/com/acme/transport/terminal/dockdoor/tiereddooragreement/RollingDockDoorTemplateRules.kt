package com.acme.transport.terminal.dockdoor.tiereddooragreement

/** A rule a rolling dock door template must pass. Check returns its problems, or none. */
interface RollingDockDoorTemplateRule {
    fun check(item: RollingDockDoorTemplate): MutableList<String>
}

data class RollingDockDoorTemplateActiveGuardRule(var limit: Double) : RollingDockDoorTemplateRule {
    override fun check(item: RollingDockDoorTemplate): MutableList<String> {
        if (item.active && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorTemplateEndsAtRangeRule(var min: Int, var max: Int) : RollingDockDoorTemplateRule {
    override fun check(item: RollingDockDoorTemplate): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class RollingDockDoorTemplateTrailerIdLengthRule(var maxLength: Int) : RollingDockDoorTemplateRule {
    override fun check(item: RollingDockDoorTemplate): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dock door template. */
data class RollingDockDoorTemplateValidator(var rules: MutableList<RollingDockDoorTemplateRule>) {
    fun validate(item: RollingDockDoorTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDockDoorTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDockDoorTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDockDoorTemplateValidator(): RollingDockDoorTemplateValidator {
    return RollingDockDoorTemplateValidator(
        mutableListOf<RollingDockDoorTemplateRule>(
            RollingDockDoorTemplateActiveGuardRule(3.6),
            RollingDockDoorTemplateEndsAtRangeRule(32, 120),
            RollingDockDoorTemplateTrailerIdLengthRule(1000),
        ),
    )
}
