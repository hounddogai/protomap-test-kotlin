package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.text.normalize

/** A rule a weighted violation rollup must pass. Check returns its problems, or none. */
interface WeightedViolationRollupRule {
    fun check(item: WeightedViolationRollup): MutableList<String>
}

data class WeightedViolationRollupCodePrefixRule(var prefix: String) : WeightedViolationRollupRule {
    override fun check(item: WeightedViolationRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRollupRequiredOwnerIdRule(var message: String) : WeightedViolationRollupRule {
    override fun check(item: WeightedViolationRollup): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRollupCodeLengthRule(var maxLength: Int) : WeightedViolationRollupRule {
    override fun check(item: WeightedViolationRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRollupAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedViolationRollupRule {
    override fun check(item: WeightedViolationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRollupRequiredNameRule(var message: String) : WeightedViolationRollupRule {
    override fun check(item: WeightedViolationRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted violation rollup. */
data class WeightedViolationRollupValidator(var rules: MutableList<WeightedViolationRollupRule>) {
    fun validate(item: WeightedViolationRollup): MutableList<String> {
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

    fun requireValid(item: WeightedViolationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedViolationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedViolationRollupValidator(): WeightedViolationRollupValidator {
    return WeightedViolationRollupValidator(
        mutableListOf<WeightedViolationRollupRule>(
            WeightedViolationRollupCodePrefixRule("X"),
            WeightedViolationRollupRequiredOwnerIdRule("weighted violation rollup needs an entry"),
            WeightedViolationRollupCodeLengthRule(80),
            WeightedViolationRollupAllowedNameRule(mutableListOf<String>("fallback")),
            WeightedViolationRollupRequiredNameRule("weighted violation rollup needs this field"),
        ),
    )
}
