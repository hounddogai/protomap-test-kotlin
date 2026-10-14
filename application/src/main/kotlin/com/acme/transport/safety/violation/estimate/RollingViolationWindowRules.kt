package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize

/** A rule a rolling violation window must pass. Check returns its problems, or none. */
interface RollingViolationWindowRule {
    fun check(item: RollingViolationWindow): MutableList<String>
}

data class RollingViolationWindowAllowedNameRule(
    var allowed: MutableList<String>,
) : RollingViolationWindowRule {
    override fun check(item: RollingViolationWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationWindowCodeLengthRule(var maxLength: Int) : RollingViolationWindowRule {
    override fun check(item: RollingViolationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationWindowRequiredRegionRule(var message: String) : RollingViolationWindowRule {
    override fun check(item: RollingViolationWindow): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation window. */
data class RollingViolationWindowValidator(var rules: MutableList<RollingViolationWindowRule>) {
    fun validate(item: RollingViolationWindow): MutableList<String> {
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

    fun requireValid(item: RollingViolationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationWindowValidator(): RollingViolationWindowValidator {
    return RollingViolationWindowValidator(
        mutableListOf<RollingViolationWindowRule>(
            RollingViolationWindowAllowedNameRule(mutableListOf<String>()),
            RollingViolationWindowCodeLengthRule(16),
            RollingViolationWindowRequiredRegionRule("rolling violation window needs this field"),
        ),
    )
}
