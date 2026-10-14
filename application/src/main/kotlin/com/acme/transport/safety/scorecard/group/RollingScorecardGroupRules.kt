package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.normalize

/** A rule a rolling scorecard group must pass. Check returns its problems, or none. */
interface RollingScorecardGroupRule {
    fun check(item: RollingScorecardGroup): MutableList<String>
}

data class RollingScorecardGroupCodePrefixRule(var prefix: String) : RollingScorecardGroupRule {
    override fun check(item: RollingScorecardGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardGroupRequiredLocaleRule(var message: String) : RollingScorecardGroupRule {
    override fun check(item: RollingScorecardGroup): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardGroupAllowedNotesRule(var allowed: MutableList<String>) : RollingScorecardGroupRule {
    override fun check(item: RollingScorecardGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling scorecard group. */
data class RollingScorecardGroupValidator(var rules: MutableList<RollingScorecardGroupRule>) {
    fun validate(item: RollingScorecardGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingScorecardGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingScorecardGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingScorecardGroupValidator(): RollingScorecardGroupValidator {
    return RollingScorecardGroupValidator(
        mutableListOf<RollingScorecardGroupRule>(
            RollingScorecardGroupCodePrefixRule("OPS"),
            RollingScorecardGroupRequiredLocaleRule("rolling scorecard group needs an entry"),
            RollingScorecardGroupAllowedNotesRule(mutableListOf<String>("fallback", "pending")),
        ),
    )
}
