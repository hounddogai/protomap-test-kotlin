package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize

/** A rule a rest break digest must pass. Check returns its problems, or none. */
interface RestBreakDigestRule {
    fun check(item: RestBreakDigest): MutableList<String>
}

data class RestBreakDigestUnitLengthRule(var maxLength: Int) : RestBreakDigestRule {
    override fun check(item: RestBreakDigest): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakDigestRequiredUnitRule(var message: String) : RestBreakDigestRule {
    override fun check(item: RestBreakDigest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakDigestCreatedAtRangeRule(var min: Int, var max: Int) : RestBreakDigestRule {
    override fun check(item: RestBreakDigest): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class RestBreakDigestAllowedLabelRule(var allowed: MutableList<String>) : RestBreakDigestRule {
    override fun check(item: RestBreakDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break digest. */
data class RestBreakDigestValidator(var rules: MutableList<RestBreakDigestRule>) {
    fun validate(item: RestBreakDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakDigestValidator(): RestBreakDigestValidator {
    return RestBreakDigestValidator(
        mutableListOf<RestBreakDigestRule>(
            RestBreakDigestUnitLengthRule(1000),
            RestBreakDigestRequiredUnitRule("rest break digest needs a value"),
            RestBreakDigestCreatedAtRangeRule(16, 32),
            RestBreakDigestAllowedLabelRule(mutableListOf<String>("lenient", "weighted", "adaptive")),
        ),
    )
}
