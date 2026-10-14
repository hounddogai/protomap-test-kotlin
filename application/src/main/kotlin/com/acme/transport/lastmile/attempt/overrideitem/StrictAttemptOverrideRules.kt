package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a strict attempt override must pass. Check returns its problems, or none. */
interface StrictAttemptOverrideRule {
    fun check(item: StrictAttemptOverride): MutableList<String>
}

data class StrictAttemptOverrideAllowedCodeRule(var allowed: MutableList<String>) : StrictAttemptOverrideRule {
    override fun check(item: StrictAttemptOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptOverrideRouteCodeLengthRule(var maxLength: Int) : StrictAttemptOverrideRule {
    override fun check(item: StrictAttemptOverride): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptOverrideRequiredCodeRule(var message: String) : StrictAttemptOverrideRule {
    override fun check(item: StrictAttemptOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptOverridePriorityRangeRule(var min: Int, var max: Int) : StrictAttemptOverrideRule {
    override fun check(item: StrictAttemptOverride): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict attempt override. */
data class StrictAttemptOverrideValidator(var rules: MutableList<StrictAttemptOverrideRule>) {
    fun validate(item: StrictAttemptOverride): MutableList<String> {
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

    fun requireValid(item: StrictAttemptOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptOverrideValidator(): StrictAttemptOverrideValidator {
    return StrictAttemptOverrideValidator(
        mutableListOf<StrictAttemptOverrideRule>(
            StrictAttemptOverrideAllowedCodeRule(mutableListOf<String>("strict", "rolling")),
            StrictAttemptOverrideRouteCodeLengthRule(16),
            StrictAttemptOverrideRequiredCodeRule("strict attempt override needs a value"),
            StrictAttemptOverridePriorityRangeRule(32, 64),
        ),
    )
}
