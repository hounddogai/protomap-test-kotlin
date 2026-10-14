package com.acme.transport.crewing.roster.digest

/** A rule a roster budget must pass. Check returns its problems, or none. */
interface RosterBudgetRule {
    fun check(item: RosterBudget): MutableList<String>
}

data class RosterBudgetCodeLengthRule(var maxLength: Int) : RosterBudgetRule {
    override fun check(item: RosterBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterBudgetShiftCodeLengthRule(var maxLength: Int) : RosterBudgetRule {
    override fun check(item: RosterBudget): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterBudgetRequiredTitleRule(var message: String) : RosterBudgetRule {
    override fun check(item: RosterBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterBudgetRequiredRegionRule(var message: String) : RosterBudgetRule {
    override fun check(item: RosterBudget): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster budget. */
data class RosterBudgetValidator(var rules: MutableList<RosterBudgetRule>) {
    fun validate(item: RosterBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterBudgetValidator(): RosterBudgetValidator {
    return RosterBudgetValidator(
        mutableListOf<RosterBudgetRule>(
            RosterBudgetCodeLengthRule(3600),
            RosterBudgetShiftCodeLengthRule(86400),
            RosterBudgetRequiredTitleRule("roster budget needs this field"),
            RosterBudgetRequiredRegionRule("roster budget needs an entry"),
        ),
    )
}
