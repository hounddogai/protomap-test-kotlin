package com.acme.transport.terminal.traileryard.budget

/** A rule a trailer yard milestone must pass. Check returns its problems, or none. */
interface TrailerYardMilestoneRule {
    fun check(item: TrailerYardMilestone): MutableList<String>
}

data class TrailerYardMilestoneRequiredCurrencyRule(var message: String) : TrailerYardMilestoneRule {
    override fun check(item: TrailerYardMilestone): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMilestoneNameLengthRule(var maxLength: Int) : TrailerYardMilestoneRule {
    override fun check(item: TrailerYardMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardMilestoneRequiredCodeRule(var message: String) : TrailerYardMilestoneRule {
    override fun check(item: TrailerYardMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard milestone. */
data class TrailerYardMilestoneValidator(var rules: MutableList<TrailerYardMilestoneRule>) {
    fun validate(item: TrailerYardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardMilestoneValidator(): TrailerYardMilestoneValidator {
    return TrailerYardMilestoneValidator(
        mutableListOf<TrailerYardMilestoneRule>(
            TrailerYardMilestoneRequiredCurrencyRule("trailer yard milestone needs an entry"),
            TrailerYardMilestoneNameLengthRule(32),
            TrailerYardMilestoneRequiredCodeRule("trailer yard milestone needs a value"),
        ),
    )
}
