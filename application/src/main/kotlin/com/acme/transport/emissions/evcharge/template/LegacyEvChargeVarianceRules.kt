package com.acme.transport.emissions.evcharge.template

/** A rule a legacy ev charge variance must pass. Check returns its problems, or none. */
interface LegacyEvChargeVarianceRule {
    fun check(item: LegacyEvChargeVariance): MutableList<String>
}

data class LegacyEvChargeVarianceTitleLengthRule(var maxLength: Int) : LegacyEvChargeVarianceRule {
    override fun check(item: LegacyEvChargeVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeVarianceCodeLengthRule(var maxLength: Int) : LegacyEvChargeVarianceRule {
    override fun check(item: LegacyEvChargeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeVarianceCodePrefixRule(var prefix: String) : LegacyEvChargeVarianceRule {
    override fun check(item: LegacyEvChargeVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyEvChargeVarianceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyEvChargeVarianceRule {
    override fun check(item: LegacyEvChargeVariance): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy ev charge variance. */
data class LegacyEvChargeVarianceValidator(var rules: MutableList<LegacyEvChargeVarianceRule>) {
    fun validate(item: LegacyEvChargeVariance): MutableList<String> {
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

    fun requireValid(item: LegacyEvChargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyEvChargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyEvChargeVarianceValidator(): LegacyEvChargeVarianceValidator {
    return LegacyEvChargeVarianceValidator(
        mutableListOf<LegacyEvChargeVarianceRule>(
            LegacyEvChargeVarianceTitleLengthRule(86400),
            LegacyEvChargeVarianceCodeLengthRule(120),
            LegacyEvChargeVarianceCodePrefixRule("Q"),
            LegacyEvChargeVarianceThresholdRangeRule(6.7, 3913.9),
        ),
    )
}
