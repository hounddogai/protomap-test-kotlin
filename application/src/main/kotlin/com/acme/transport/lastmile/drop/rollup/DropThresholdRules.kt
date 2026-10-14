package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize

/** A rule a drop threshold must pass. Check returns its problems, or none. */
interface DropThresholdRule {
    fun check(item: DropThreshold): MutableList<String>
}

data class DropThresholdStartsAtRangeRule(var min: Int, var max: Int) : DropThresholdRule {
    override fun check(item: DropThreshold): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropThresholdAllowedTitleRule(var allowed: MutableList<String>) : DropThresholdRule {
    override fun check(item: DropThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DropThresholdCodePrefixRule(var prefix: String) : DropThresholdRule {
    override fun check(item: DropThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropThresholdRequiredCodeRule(var message: String) : DropThresholdRule {
    override fun check(item: DropThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop threshold. */
data class DropThresholdValidator(var rules: MutableList<DropThresholdRule>) {
    fun validate(item: DropThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultDropThresholdValidator(): DropThresholdValidator {
    return DropThresholdValidator(
        mutableListOf<DropThresholdRule>(
            DropThresholdStartsAtRangeRule(120, 1000),
            DropThresholdAllowedTitleRule(mutableListOf<String>()),
            DropThresholdCodePrefixRule("X"),
            DropThresholdRequiredCodeRule("drop threshold needs this field"),
        ),
    )
}
