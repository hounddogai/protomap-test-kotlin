package com.acme.transport.safety.coaching.estimate

/** A rule a coaching rollup must pass. Check returns its problems, or none. */
interface CoachingRollupRule {
    fun check(item: CoachingRollup): MutableList<String>
}

data class CoachingRollupRequiredCodeRule(var message: String) : CoachingRollupRule {
    override fun check(item: CoachingRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingRollupBudgetRangeRule(var min: Double, var max: Double) : CoachingRollupRule {
    override fun check(item: CoachingRollup): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class CoachingRollupCodeLengthRule(var maxLength: Int) : CoachingRollupRule {
    override fun check(item: CoachingRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingRollupRequiredContactEmailRule(var message: String) : CoachingRollupRule {
    override fun check(item: CoachingRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching rollup. */
data class CoachingRollupValidator(var rules: MutableList<CoachingRollupRule>) {
    fun validate(item: CoachingRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: CoachingRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingRollupValidator(): CoachingRollupValidator {
    return CoachingRollupValidator(
        mutableListOf<CoachingRollupRule>(
            CoachingRollupRequiredCodeRule("coaching rollup needs a value"),
            CoachingRollupBudgetRangeRule(4.2, 3669.2),
            CoachingRollupCodeLengthRule(64),
            CoachingRollupRequiredContactEmailRule("coaching rollup needs an entry"),
        ),
    )
}
