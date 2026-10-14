package com.acme.transport.crewing.restbreak.tier

/** A rule a default rest break score must pass. Check returns its problems, or none. */
interface DefaultRestBreakScoreRule {
    fun check(item: DefaultRestBreakScore): MutableList<String>
}

data class DefaultRestBreakScoreRequiredShiftCodeRule(var message: String) : DefaultRestBreakScoreRule {
    override fun check(item: DefaultRestBreakScore): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScoreRequiredTitleRule(var message: String) : DefaultRestBreakScoreRule {
    override fun check(item: DefaultRestBreakScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakScoreApprovedGuardRule(var limit: Int) : DefaultRestBreakScoreRule {
    override fun check(item: DefaultRestBreakScore): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rest break score. */
data class DefaultRestBreakScoreValidator(var rules: MutableList<DefaultRestBreakScoreRule>) {
    fun validate(item: DefaultRestBreakScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRestBreakScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRestBreakScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRestBreakScoreValidator(): DefaultRestBreakScoreValidator {
    return DefaultRestBreakScoreValidator(
        mutableListOf<DefaultRestBreakScoreRule>(
            DefaultRestBreakScoreRequiredShiftCodeRule("default rest break score needs a value"),
            DefaultRestBreakScoreRequiredTitleRule("default rest break score needs a value"),
            DefaultRestBreakScoreApprovedGuardRule(1000),
        ),
    )
}
