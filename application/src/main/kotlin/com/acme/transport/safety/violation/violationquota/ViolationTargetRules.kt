package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize

/** A rule a violation target must pass. Check returns its problems, or none. */
interface ViolationTargetRule {
    fun check(item: ViolationTarget): MutableList<String>
}

data class ViolationTargetLabelLengthRule(var maxLength: Int) : ViolationTargetRule {
    override fun check(item: ViolationTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationTargetAllowedCodeRule(var allowed: MutableList<String>) : ViolationTargetRule {
    override fun check(item: ViolationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ViolationTargetRequiredShiftCodeRule(var message: String) : ViolationTargetRule {
    override fun check(item: ViolationTarget): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation target. */
data class ViolationTargetValidator(var rules: MutableList<ViolationTargetRule>) {
    fun validate(item: ViolationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationTargetValidator(): ViolationTargetValidator {
    return ViolationTargetValidator(
        mutableListOf<ViolationTargetRule>(
            ViolationTargetLabelLengthRule(16),
            ViolationTargetAllowedCodeRule(mutableListOf<String>("regional")),
            ViolationTargetRequiredShiftCodeRule("violation target needs an entry"),
        ),
    )
}
