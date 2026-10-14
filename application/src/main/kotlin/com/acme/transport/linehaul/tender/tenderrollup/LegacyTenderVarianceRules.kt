package com.acme.transport.linehaul.tender.tenderrollup

/** A rule a legacy tender variance must pass. Check returns its problems, or none. */
interface LegacyTenderVarianceRule {
    fun check(item: LegacyTenderVariance): MutableList<String>
}

data class LegacyTenderVarianceCreatedAtRangeRule(var min: Int, var max: Int) : LegacyTenderVarianceRule {
    override fun check(item: LegacyTenderVariance): MutableList<String> {
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

data class LegacyTenderVarianceLabelLengthRule(var maxLength: Int) : LegacyTenderVarianceRule {
    override fun check(item: LegacyTenderVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderVarianceCodePrefixRule(var prefix: String) : LegacyTenderVarianceRule {
    override fun check(item: LegacyTenderVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tender variance. */
data class LegacyTenderVarianceValidator(var rules: MutableList<LegacyTenderVarianceRule>) {
    fun validate(item: LegacyTenderVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTenderVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTenderVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTenderVarianceValidator(): LegacyTenderVarianceValidator {
    return LegacyTenderVarianceValidator(
        mutableListOf<LegacyTenderVarianceRule>(
            LegacyTenderVarianceCreatedAtRangeRule(64, 255),
            LegacyTenderVarianceLabelLengthRule(64),
            LegacyTenderVarianceCodePrefixRule("ACM"),
        ),
    )
}
