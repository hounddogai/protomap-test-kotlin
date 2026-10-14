package com.acme.transport.safety.coaching.bundle

/** A rule a coaching group must pass. Check returns its problems, or none. */
interface CoachingGroupRule {
    fun check(item: CoachingGroup): MutableList<String>
}

data class CoachingGroupCodePrefixRule(var prefix: String) : CoachingGroupRule {
    override fun check(item: CoachingGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingGroupCodeLengthRule(var maxLength: Int) : CoachingGroupRule {
    override fun check(item: CoachingGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingGroupRequiredCodeRule(var message: String) : CoachingGroupRule {
    override fun check(item: CoachingGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching group. */
data class CoachingGroupValidator(var rules: MutableList<CoachingGroupRule>) {
    fun validate(item: CoachingGroup): MutableList<String> {
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

    fun requireValid(item: CoachingGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingGroupValidator(): CoachingGroupValidator {
    return CoachingGroupValidator(
        mutableListOf<CoachingGroupRule>(
            CoachingGroupCodePrefixRule("REF"),
            CoachingGroupCodeLengthRule(32),
            CoachingGroupRequiredCodeRule("coaching group needs a value"),
        ),
    )
}
