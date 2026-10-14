package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize

/** A rule a manual violation window must pass. Check returns its problems, or none. */
interface ManualViolationWindowRule {
    fun check(item: ManualViolationWindow): MutableList<String>
}

data class ManualViolationWindowCodePrefixRule(var prefix: String) : ManualViolationWindowRule {
    override fun check(item: ManualViolationWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationWindowRetriesRangeRule(var min: Int, var max: Int) : ManualViolationWindowRule {
    override fun check(item: ManualViolationWindow): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationWindowAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : ManualViolationWindowRule {
    override fun check(item: ManualViolationWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationWindowPriorityRangeRule(var min: Int, var max: Int) : ManualViolationWindowRule {
    override fun check(item: ManualViolationWindow): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual violation window. */
data class ManualViolationWindowValidator(var rules: MutableList<ManualViolationWindowRule>) {
    fun validate(item: ManualViolationWindow): MutableList<String> {
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

    fun requireValid(item: ManualViolationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualViolationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualViolationWindowValidator(): ManualViolationWindowValidator {
    return ManualViolationWindowValidator(
        mutableListOf<ManualViolationWindowRule>(
            ManualViolationWindowCodePrefixRule("OPS"),
            ManualViolationWindowRetriesRangeRule(16, 32),
            ManualViolationWindowAllowedDescriptionRule(mutableListOf<String>("rolling", "strict")),
            ManualViolationWindowPriorityRangeRule(120, 3600),
        ),
    )
}
