package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.normalize

/** A rule a default trailer allocation must pass. Check returns its problems, or none. */
interface DefaultTrailerAllocationRule {
    fun check(item: DefaultTrailerAllocation): MutableList<String>
}

data class DefaultTrailerAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : DefaultTrailerAllocationRule {
    override fun check(item: DefaultTrailerAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerAllocationRequiredNameRule(var message: String) : DefaultTrailerAllocationRule {
    override fun check(item: DefaultTrailerAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerAllocationCodePrefixRule(var prefix: String) : DefaultTrailerAllocationRule {
    override fun check(item: DefaultTrailerAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerAllocationScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTrailerAllocationRule {
    override fun check(item: DefaultTrailerAllocation): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default trailer allocation. */
data class DefaultTrailerAllocationValidator(var rules: MutableList<DefaultTrailerAllocationRule>) {
    fun validate(item: DefaultTrailerAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTrailerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerAllocationValidator(): DefaultTrailerAllocationValidator {
    return DefaultTrailerAllocationValidator(
        mutableListOf<DefaultTrailerAllocationRule>(
            DefaultTrailerAllocationAllowedCodeRule(mutableListOf<String>()),
            DefaultTrailerAllocationRequiredNameRule("default trailer allocation needs an entry"),
            DefaultTrailerAllocationCodePrefixRule("REF"),
            DefaultTrailerAllocationScoreRangeRule(9.8, 885.1),
        ),
    )
}
