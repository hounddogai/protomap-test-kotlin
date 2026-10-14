package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** A rule a roster group must pass. Check returns its problems, or none. */
interface RosterGroupRule {
    fun check(item: RosterGroup): MutableList<String>
}

data class RosterGroupEndsAtRangeRule(var min: Int, var max: Int) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterGroupAllowedSummaryRule(var allowed: MutableList<String>) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RosterGroupRequiredSummaryRule(var message: String) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterGroupCodeLengthRule(var maxLength: Int) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterGroupTitleLengthRule(var maxLength: Int) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterGroupRequiredCodeRule(var message: String) : RosterGroupRule {
    override fun check(item: RosterGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster group. */
data class RosterGroupValidator(var rules: MutableList<RosterGroupRule>) {
    fun validate(item: RosterGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterGroupValidator(): RosterGroupValidator {
    return RosterGroupValidator(
        mutableListOf<RosterGroupRule>(
            RosterGroupEndsAtRangeRule(32, 120),
            RosterGroupAllowedSummaryRule(mutableListOf<String>("capped", "manual", "tiered")),
            RosterGroupRequiredSummaryRule("roster group needs this field"),
            RosterGroupCodeLengthRule(86400),
            RosterGroupTitleLengthRule(32),
            RosterGroupRequiredCodeRule("roster group needs an entry"),
        ),
    )
}
