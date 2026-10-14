package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.text.normalize

/** A rule a violation rollup must pass. Check returns its problems, or none. */
interface ViolationRollupRule {
    fun check(item: ViolationRollup): MutableList<String>
}

data class ViolationRollupRequiredCodeRule(var message: String) : ViolationRollupRule {
    override fun check(item: ViolationRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationRollupCodeLengthRule(var maxLength: Int) : ViolationRollupRule {
    override fun check(item: ViolationRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationRollupReviewerLengthRule(var maxLength: Int) : ViolationRollupRule {
    override fun check(item: ViolationRollup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationRollupAllowedCodeRule(var allowed: MutableList<String>) : ViolationRollupRule {
    override fun check(item: ViolationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation rollup. */
data class ViolationRollupValidator(var rules: MutableList<ViolationRollupRule>) {
    fun validate(item: ViolationRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationRollupValidator(): ViolationRollupValidator {
    return ViolationRollupValidator(
        mutableListOf<ViolationRollupRule>(
            ViolationRollupRequiredCodeRule("violation rollup needs this field"),
            ViolationRollupCodeLengthRule(16),
            ViolationRollupReviewerLengthRule(80),
            ViolationRollupAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
