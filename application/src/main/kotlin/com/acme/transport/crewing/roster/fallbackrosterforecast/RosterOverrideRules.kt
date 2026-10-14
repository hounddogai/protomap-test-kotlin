package com.acme.transport.crewing.roster.fallbackrosterforecast

/** A rule a roster override must pass. Check returns its problems, or none. */
interface RosterOverrideRule {
    fun check(item: RosterOverride): MutableList<String>
}

data class RosterOverrideRequiredCodeRule(var message: String) : RosterOverrideRule {
    override fun check(item: RosterOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterOverrideRequiredTrailerIdRule(var message: String) : RosterOverrideRule {
    override fun check(item: RosterOverride): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterOverrideLabelLengthRule(var maxLength: Int) : RosterOverrideRule {
    override fun check(item: RosterOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster override. */
data class RosterOverrideValidator(var rules: MutableList<RosterOverrideRule>) {
    fun validate(item: RosterOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterOverrideValidator(): RosterOverrideValidator {
    return RosterOverrideValidator(
        mutableListOf<RosterOverrideRule>(
            RosterOverrideRequiredCodeRule("roster override needs this field"),
            RosterOverrideRequiredTrailerIdRule("roster override needs an entry"),
            RosterOverrideLabelLengthRule(3600),
        ),
    )
}
