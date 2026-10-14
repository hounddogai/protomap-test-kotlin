package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.text.normalize

/** A rule a violation draft must pass. Check returns its problems, or none. */
interface ViolationDraftRule {
    fun check(item: ViolationDraft): MutableList<String>
}

data class ViolationDraftCodeLengthRule(var maxLength: Int) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDraftAllowedRegionRule(var allowed: MutableList<String>) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDraftCeilingRangeRule(var min: Double, var max: Double) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ViolationDraftPinnedGuardRule(var limit: Int) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        if (item.pinned && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDraftContactEmailLengthRule(var maxLength: Int) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationDraftLabelLengthRule(var maxLength: Int) : ViolationDraftRule {
    override fun check(item: ViolationDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation draft. */
data class ViolationDraftValidator(var rules: MutableList<ViolationDraftRule>) {
    fun validate(item: ViolationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationDraftValidator(): ViolationDraftValidator {
    return ViolationDraftValidator(
        mutableListOf<ViolationDraftRule>(
            ViolationDraftCodeLengthRule(32),
            ViolationDraftAllowedRegionRule(mutableListOf<String>("scheduled")),
            ViolationDraftCeilingRangeRule(3.9, 1885.5),
            ViolationDraftPinnedGuardRule(16),
            ViolationDraftContactEmailLengthRule(1000),
            ViolationDraftLabelLengthRule(3600),
        ),
    )
}
