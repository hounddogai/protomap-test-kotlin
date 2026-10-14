package com.acme.transport.emissions.idling.review

/** A rule a global idling policy must pass. Check returns its problems, or none. */
interface GlobalIdlingPolicyRule {
    fun check(item: GlobalIdlingPolicy): MutableList<String>
}

data class GlobalIdlingPolicyRequiredRouteCodeRule(var message: String) : GlobalIdlingPolicyRule {
    override fun check(item: GlobalIdlingPolicy): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingPolicyCreatedAtRangeRule(var min: Int, var max: Int) : GlobalIdlingPolicyRule {
    override fun check(item: GlobalIdlingPolicy): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingPolicyTitleLengthRule(var maxLength: Int) : GlobalIdlingPolicyRule {
    override fun check(item: GlobalIdlingPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingPolicyRequiredCodeRule(var message: String) : GlobalIdlingPolicyRule {
    override fun check(item: GlobalIdlingPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global idling policy. */
data class GlobalIdlingPolicyValidator(var rules: MutableList<GlobalIdlingPolicyRule>) {
    fun validate(item: GlobalIdlingPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalIdlingPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIdlingPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIdlingPolicyValidator(): GlobalIdlingPolicyValidator {
    return GlobalIdlingPolicyValidator(
        mutableListOf<GlobalIdlingPolicyRule>(
            GlobalIdlingPolicyRequiredRouteCodeRule("global idling policy needs a value"),
            GlobalIdlingPolicyCreatedAtRangeRule(16, 16),
            GlobalIdlingPolicyTitleLengthRule(32),
            GlobalIdlingPolicyRequiredCodeRule("global idling policy needs an entry"),
        ),
    )
}
