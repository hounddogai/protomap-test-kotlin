package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.normalize

/** A rule a locker variance must pass. Check returns its problems, or none. */
interface LockerVarianceRule {
    fun check(item: LockerVariance): MutableList<String>
}

data class LockerVarianceCodeLengthRule(var maxLength: Int) : LockerVarianceRule {
    override fun check(item: LockerVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerVarianceRequiredTitleRule(var message: String) : LockerVarianceRule {
    override fun check(item: LockerVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerVarianceLimitRangeRule(var min: Int, var max: Int) : LockerVarianceRule {
    override fun check(item: LockerVariance): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LockerVarianceExpiresAtRangeRule(var min: Int, var max: Int) : LockerVarianceRule {
    override fun check(item: LockerVariance): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class LockerVarianceAllowedTitleRule(var allowed: MutableList<String>) : LockerVarianceRule {
    override fun check(item: LockerVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker variance. */
data class LockerVarianceValidator(var rules: MutableList<LockerVarianceRule>) {
    fun validate(item: LockerVariance): MutableList<String> {
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

    fun requireValid(item: LockerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerVarianceValidator(): LockerVarianceValidator {
    return LockerVarianceValidator(
        mutableListOf<LockerVarianceRule>(
            LockerVarianceCodeLengthRule(32),
            LockerVarianceRequiredTitleRule("locker variance needs an entry"),
            LockerVarianceLimitRangeRule(16, 86400),
            LockerVarianceExpiresAtRangeRule(16, 64),
            LockerVarianceAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
