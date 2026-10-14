package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.kit.text.normalize

/** A rule a attempt variance must pass. Check returns its problems, or none. */
interface AttemptVarianceRule {
    fun check(item: AttemptVariance): MutableList<String>
}

data class AttemptVarianceNameLengthRule(var maxLength: Int) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptVarianceAllowedNameRule(var allowed: MutableList<String>) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AttemptVarianceQuantityRangeRule(var min: Int, var max: Int) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class AttemptVarianceRequiredNameRule(var message: String) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptVarianceRequiredDriverIdRule(var message: String) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptVarianceRequiredCodeRule(var message: String) : AttemptVarianceRule {
    override fun check(item: AttemptVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt variance. */
data class AttemptVarianceValidator(var rules: MutableList<AttemptVarianceRule>) {
    fun validate(item: AttemptVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptVarianceValidator(): AttemptVarianceValidator {
    return AttemptVarianceValidator(
        mutableListOf<AttemptVarianceRule>(
            AttemptVarianceNameLengthRule(3600),
            AttemptVarianceAllowedNameRule(mutableListOf<String>("capped", "lenient")),
            AttemptVarianceQuantityRangeRule(80, 255),
            AttemptVarianceRequiredNameRule("attempt variance needs this field"),
            AttemptVarianceRequiredDriverIdRule("attempt variance needs a value"),
            AttemptVarianceRequiredCodeRule("attempt variance needs this field"),
        ),
    )
}
