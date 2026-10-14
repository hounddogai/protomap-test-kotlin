package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize

/** A rule a violation group must pass. Check returns its problems, or none. */
interface ViolationGroupRule {
    fun check(item: ViolationGroup): MutableList<String>
}

data class ViolationGroupAllowedCodeRule(var allowed: MutableList<String>) : ViolationGroupRule {
    override fun check(item: ViolationGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ViolationGroupOdometerRangeRule(var min: Int, var max: Int) : ViolationGroupRule {
    override fun check(item: ViolationGroup): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationGroupEndsAtRangeRule(var min: Int, var max: Int) : ViolationGroupRule {
    override fun check(item: ViolationGroup): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationGroupMaximumRangeRule(var min: Double, var max: Double) : ViolationGroupRule {
    override fun check(item: ViolationGroup): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ViolationGroupRequiredTitleRule(var message: String) : ViolationGroupRule {
    override fun check(item: ViolationGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation group. */
data class ViolationGroupValidator(var rules: MutableList<ViolationGroupRule>) {
    fun validate(item: ViolationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationGroupValidator(): ViolationGroupValidator {
    return ViolationGroupValidator(
        mutableListOf<ViolationGroupRule>(
            ViolationGroupAllowedCodeRule(mutableListOf<String>()),
            ViolationGroupOdometerRangeRule(120, 1000),
            ViolationGroupEndsAtRangeRule(16, 86400),
            ViolationGroupMaximumRangeRule(2.9, 1367.3),
            ViolationGroupRequiredTitleRule("violation group needs an entry"),
        ),
    )
}
