package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize

/** A rule a trailer override must pass. Check returns its problems, or none. */
interface TrailerOverrideRule {
    fun check(item: TrailerOverride): MutableList<String>
}

data class TrailerOverrideAllowedUnitRule(var allowed: MutableList<String>) : TrailerOverrideRule {
    override fun check(item: TrailerOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TrailerOverrideUnitLengthRule(var maxLength: Int) : TrailerOverrideRule {
    override fun check(item: TrailerOverride): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerOverrideCodePrefixRule(var prefix: String) : TrailerOverrideRule {
    override fun check(item: TrailerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerOverrideCodeLengthRule(var maxLength: Int) : TrailerOverrideRule {
    override fun check(item: TrailerOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer override. */
data class TrailerOverrideValidator(var rules: MutableList<TrailerOverrideRule>) {
    fun validate(item: TrailerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerOverrideValidator(): TrailerOverrideValidator {
    return TrailerOverrideValidator(
        mutableListOf<TrailerOverrideRule>(
            TrailerOverrideAllowedUnitRule(mutableListOf<String>("batched", "scheduled")),
            TrailerOverrideUnitLengthRule(86400),
            TrailerOverrideCodePrefixRule("Q"),
            TrailerOverrideCodeLengthRule(80),
        ),
    )
}
