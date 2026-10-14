package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a violation window must pass. Check returns its problems, or none. */
interface ViolationWindowRule {
    fun check(item: ViolationWindow): MutableList<String>
}

data class ViolationWindowAllowedNameRule(var allowed: MutableList<String>) : ViolationWindowRule {
    override fun check(item: ViolationWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ViolationWindowRequiredTitleRule(var message: String) : ViolationWindowRule {
    override fun check(item: ViolationWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationWindowCodeLengthRule(var maxLength: Int) : ViolationWindowRule {
    override fun check(item: ViolationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationWindowThresholdRangeRule(var min: Double, var max: Double) : ViolationWindowRule {
    override fun check(item: ViolationWindow): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a violation window. */
data class ViolationWindowValidator(var rules: MutableList<ViolationWindowRule>) {
    fun validate(item: ViolationWindow): MutableList<String> {
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

    fun requireValid(item: ViolationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationWindowValidator(): ViolationWindowValidator {
    return ViolationWindowValidator(
        mutableListOf<ViolationWindowRule>(
            ViolationWindowAllowedNameRule(mutableListOf<String>()),
            ViolationWindowRequiredTitleRule("violation window needs a value"),
            ViolationWindowCodeLengthRule(1000),
            ViolationWindowThresholdRangeRule(5.0, 1169.5),
        ),
    )
}
