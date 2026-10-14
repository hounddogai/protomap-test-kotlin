package com.acme.transport.crewing.roster.plan

/** A rule a pending roster plan must pass. Check returns its problems, or none. */
interface PendingRosterPlanRule {
    fun check(item: PendingRosterPlan): MutableList<String>
}

data class PendingRosterPlanRequiredCodeRule(var message: String) : PendingRosterPlanRule {
    override fun check(item: PendingRosterPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPlanCodePrefixRule(var prefix: String) : PendingRosterPlanRule {
    override fun check(item: PendingRosterPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPlanCodeLengthRule(var maxLength: Int) : PendingRosterPlanRule {
    override fun check(item: PendingRosterPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterPlanRequiredCountryRule(var message: String) : PendingRosterPlanRule {
    override fun check(item: PendingRosterPlan): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending roster plan. */
data class PendingRosterPlanValidator(var rules: MutableList<PendingRosterPlanRule>) {
    fun validate(item: PendingRosterPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRosterPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterPlanValidator(): PendingRosterPlanValidator {
    return PendingRosterPlanValidator(
        mutableListOf<PendingRosterPlanRule>(
            PendingRosterPlanRequiredCodeRule("pending roster plan needs an entry"),
            PendingRosterPlanCodePrefixRule("X"),
            PendingRosterPlanCodeLengthRule(255),
            PendingRosterPlanRequiredCountryRule("pending roster plan needs an entry"),
        ),
    )
}
