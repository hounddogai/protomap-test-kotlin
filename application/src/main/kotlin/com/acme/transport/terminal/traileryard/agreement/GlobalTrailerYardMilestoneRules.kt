package com.acme.transport.terminal.traileryard.agreement

/** A rule a global trailer yard milestone must pass. Check returns its problems, or none. */
interface GlobalTrailerYardMilestoneRule {
    fun check(item: GlobalTrailerYardMilestone): MutableList<String>
}

data class GlobalTrailerYardMilestoneRetriesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTrailerYardMilestoneRule {
    override fun check(item: GlobalTrailerYardMilestone): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerYardMilestoneRequiredCodeRule(var message: String) : GlobalTrailerYardMilestoneRule {
    override fun check(item: GlobalTrailerYardMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardMilestoneCodeLengthRule(var maxLength: Int) : GlobalTrailerYardMilestoneRule {
    override fun check(item: GlobalTrailerYardMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardMilestoneExternalRefLengthRule(
    var maxLength: Int,
) : GlobalTrailerYardMilestoneRule {
    override fun check(item: GlobalTrailerYardMilestone): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer yard milestone. */
data class GlobalTrailerYardMilestoneValidator(var rules: MutableList<GlobalTrailerYardMilestoneRule>) {
    fun validate(item: GlobalTrailerYardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTrailerYardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerYardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerYardMilestoneValidator(): GlobalTrailerYardMilestoneValidator {
    return GlobalTrailerYardMilestoneValidator(
        mutableListOf<GlobalTrailerYardMilestoneRule>(
            GlobalTrailerYardMilestoneRetriesRangeRule(255, 3600),
            GlobalTrailerYardMilestoneRequiredCodeRule("global trailer yard milestone needs a value"),
            GlobalTrailerYardMilestoneCodeLengthRule(86400),
            GlobalTrailerYardMilestoneExternalRefLengthRule(120),
        ),
    )
}
