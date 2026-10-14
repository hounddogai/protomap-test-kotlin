package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize

/** A rule a pending violation rollup must pass. Check returns its problems, or none. */
interface PendingViolationRollupRule {
    fun check(item: PendingViolationRollup): MutableList<String>
}

data class PendingViolationRollupAttemptsRangeRule(var min: Int, var max: Int) : PendingViolationRollupRule {
    override fun check(item: PendingViolationRollup): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class PendingViolationRollupAllowedNameRule(
    var allowed: MutableList<String>,
) : PendingViolationRollupRule {
    override fun check(item: PendingViolationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationRollupCodePrefixRule(var prefix: String) : PendingViolationRollupRule {
    override fun check(item: PendingViolationRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationRollupRequiredCodeRule(var message: String) : PendingViolationRollupRule {
    override fun check(item: PendingViolationRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingViolationRollupCodeLengthRule(var maxLength: Int) : PendingViolationRollupRule {
    override fun check(item: PendingViolationRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending violation rollup. */
data class PendingViolationRollupValidator(var rules: MutableList<PendingViolationRollupRule>) {
    fun validate(item: PendingViolationRollup): MutableList<String> {
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

    fun requireValid(item: PendingViolationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingViolationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingViolationRollupValidator(): PendingViolationRollupValidator {
    return PendingViolationRollupValidator(
        mutableListOf<PendingViolationRollupRule>(
            PendingViolationRollupAttemptsRangeRule(16, 32),
            PendingViolationRollupAllowedNameRule(mutableListOf<String>()),
            PendingViolationRollupCodePrefixRule("OPS"),
            PendingViolationRollupRequiredCodeRule("pending violation rollup needs this field"),
            PendingViolationRollupCodeLengthRule(80),
        ),
    )
}
