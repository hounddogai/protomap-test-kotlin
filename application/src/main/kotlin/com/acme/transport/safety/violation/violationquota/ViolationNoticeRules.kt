package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize

/** A rule a violation notice must pass. Check returns its problems, or none. */
interface ViolationNoticeRule {
    fun check(item: ViolationNotice): MutableList<String>
}

data class ViolationNoticeAllowedCodeRule(var allowed: MutableList<String>) : ViolationNoticeRule {
    override fun check(item: ViolationNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ViolationNoticeMaximumRangeRule(var min: Double, var max: Double) : ViolationNoticeRule {
    override fun check(item: ViolationNotice): MutableList<String> {
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

data class ViolationNoticeRequiredLabelRule(var message: String) : ViolationNoticeRule {
    override fun check(item: ViolationNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation notice. */
data class ViolationNoticeValidator(var rules: MutableList<ViolationNoticeRule>) {
    fun validate(item: ViolationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationNoticeValidator(): ViolationNoticeValidator {
    return ViolationNoticeValidator(
        mutableListOf<ViolationNoticeRule>(
            ViolationNoticeAllowedCodeRule(mutableListOf<String>("seasonal", "global", "primary")),
            ViolationNoticeMaximumRangeRule(6.5, 1135.7),
            ViolationNoticeRequiredLabelRule("violation notice needs a value"),
        ),
    )
}
