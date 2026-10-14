package com.acme.transport.lastmile.redelivery.entry

/** A rule a redelivery bundle must pass. Check returns its problems, or none. */
interface RedeliveryBundleRule {
    fun check(item: RedeliveryBundle): MutableList<String>
}

data class RedeliveryBundleCodeLengthRule(var maxLength: Int) : RedeliveryBundleRule {
    override fun check(item: RedeliveryBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBundleCountryLengthRule(var maxLength: Int) : RedeliveryBundleRule {
    override fun check(item: RedeliveryBundle): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryBundlePalletsRangeRule(var min: Int, var max: Int) : RedeliveryBundleRule {
    override fun check(item: RedeliveryBundle): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RedeliveryBundleLaneLengthRule(var maxLength: Int) : RedeliveryBundleRule {
    override fun check(item: RedeliveryBundle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery bundle. */
data class RedeliveryBundleValidator(var rules: MutableList<RedeliveryBundleRule>) {
    fun validate(item: RedeliveryBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryBundleValidator(): RedeliveryBundleValidator {
    return RedeliveryBundleValidator(
        mutableListOf<RedeliveryBundleRule>(
            RedeliveryBundleCodeLengthRule(64),
            RedeliveryBundleCountryLengthRule(3600),
            RedeliveryBundlePalletsRangeRule(16, 86400),
            RedeliveryBundleLaneLengthRule(16),
        ),
    )
}
