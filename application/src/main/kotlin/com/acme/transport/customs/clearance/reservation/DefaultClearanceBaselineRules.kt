package com.acme.transport.customs.clearance.reservation

/** A rule a default clearance baseline must pass. Check returns its problems, or none. */
interface DefaultClearanceBaselineRule {
    fun check(item: DefaultClearanceBaseline): MutableList<String>
}

data class DefaultClearanceBaselineQuantityRangeRule(
    var min: Int,
    var max: Int,
) : DefaultClearanceBaselineRule {
    override fun check(item: DefaultClearanceBaseline): MutableList<String> {
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

data class DefaultClearanceBaselineNameLengthRule(var maxLength: Int) : DefaultClearanceBaselineRule {
    override fun check(item: DefaultClearanceBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceBaselineRegionLengthRule(var maxLength: Int) : DefaultClearanceBaselineRule {
    override fun check(item: DefaultClearanceBaseline): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default clearance baseline. */
data class DefaultClearanceBaselineValidator(var rules: MutableList<DefaultClearanceBaselineRule>) {
    fun validate(item: DefaultClearanceBaseline): MutableList<String> {
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

    fun requireValid(item: DefaultClearanceBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultClearanceBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultClearanceBaselineValidator(): DefaultClearanceBaselineValidator {
    return DefaultClearanceBaselineValidator(
        mutableListOf<DefaultClearanceBaselineRule>(
            DefaultClearanceBaselineQuantityRangeRule(3600, 86400),
            DefaultClearanceBaselineNameLengthRule(64),
            DefaultClearanceBaselineRegionLengthRule(3600),
        ),
    )
}
