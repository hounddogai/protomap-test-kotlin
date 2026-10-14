package com.acme.transport.lastmile.attempt.claim

/** A rule a primary attempt bundle must pass. Check returns its problems, or none. */
interface PrimaryAttemptBundleRule {
    fun check(item: PrimaryAttemptBundle): MutableList<String>
}

data class PrimaryAttemptBundleCodeLengthRule(var maxLength: Int) : PrimaryAttemptBundleRule {
    override fun check(item: PrimaryAttemptBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptBundleBudgetRangeRule(var min: Double, var max: Double) : PrimaryAttemptBundleRule {
    override fun check(item: PrimaryAttemptBundle): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptBundleCurrencyLengthRule(var maxLength: Int) : PrimaryAttemptBundleRule {
    override fun check(item: PrimaryAttemptBundle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptBundleQuantityRangeRule(var min: Int, var max: Int) : PrimaryAttemptBundleRule {
    override fun check(item: PrimaryAttemptBundle): MutableList<String> {
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

/** Runs every rule of a primary attempt bundle. */
data class PrimaryAttemptBundleValidator(var rules: MutableList<PrimaryAttemptBundleRule>) {
    fun validate(item: PrimaryAttemptBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAttemptBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAttemptBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAttemptBundleValidator(): PrimaryAttemptBundleValidator {
    return PrimaryAttemptBundleValidator(
        mutableListOf<PrimaryAttemptBundleRule>(
            PrimaryAttemptBundleCodeLengthRule(64),
            PrimaryAttemptBundleBudgetRangeRule(7.5, 3739.2),
            PrimaryAttemptBundleCurrencyLengthRule(16),
            PrimaryAttemptBundleQuantityRangeRule(255, 86400),
        ),
    )
}
