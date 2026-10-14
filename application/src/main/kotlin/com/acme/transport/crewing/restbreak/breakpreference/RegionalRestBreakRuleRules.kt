package com.acme.transport.crewing.restbreak.breakpreference

/** A rule a regional rest break rule must pass. Check returns its problems, or none. */
interface RegionalRestBreakRuleRule {
    fun check(item: RegionalRestBreakRule): MutableList<String>
}

data class RegionalRestBreakRuleTrailerIdLengthRule(var maxLength: Int) : RegionalRestBreakRuleRule {
    override fun check(item: RegionalRestBreakRule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRestBreakRuleLabelLengthRule(var maxLength: Int) : RegionalRestBreakRuleRule {
    override fun check(item: RegionalRestBreakRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRestBreakRuleOwnerIdLengthRule(var maxLength: Int) : RegionalRestBreakRuleRule {
    override fun check(item: RegionalRestBreakRule): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRestBreakRuleDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRestBreakRuleRule {
    override fun check(item: RegionalRestBreakRule): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional rest break rule. */
data class RegionalRestBreakRuleValidator(var rules: MutableList<RegionalRestBreakRuleRule>) {
    fun validate(item: RegionalRestBreakRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRestBreakRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRestBreakRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRestBreakRuleValidator(): RegionalRestBreakRuleValidator {
    return RegionalRestBreakRuleValidator(
        mutableListOf<RegionalRestBreakRuleRule>(
            RegionalRestBreakRuleTrailerIdLengthRule(80),
            RegionalRestBreakRuleLabelLengthRule(80),
            RegionalRestBreakRuleOwnerIdLengthRule(32),
            RegionalRestBreakRuleDurationMinutesRangeRule(16, 80),
        ),
    )
}
