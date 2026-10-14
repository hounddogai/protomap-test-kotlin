package com.acme.transport.freightpay.ratecard.cardgroup

/** A rule a legacy rate card baseline must pass. Check returns its problems, or none. */
interface LegacyRateCardBaselineRule {
    fun check(item: LegacyRateCardBaseline): MutableList<String>
}

data class LegacyRateCardBaselineCodeLengthRule(var maxLength: Int) : LegacyRateCardBaselineRule {
    override fun check(item: LegacyRateCardBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardBaselineCodePrefixRule(var prefix: String) : LegacyRateCardBaselineRule {
    override fun check(item: LegacyRateCardBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardBaselineNameLengthRule(var maxLength: Int) : LegacyRateCardBaselineRule {
    override fun check(item: LegacyRateCardBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardBaselineStopCountRangeRule(var min: Int, var max: Int) : LegacyRateCardBaselineRule {
    override fun check(item: LegacyRateCardBaseline): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rate card baseline. */
data class LegacyRateCardBaselineValidator(var rules: MutableList<LegacyRateCardBaselineRule>) {
    fun validate(item: LegacyRateCardBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRateCardBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRateCardBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRateCardBaselineValidator(): LegacyRateCardBaselineValidator {
    return LegacyRateCardBaselineValidator(
        mutableListOf<LegacyRateCardBaselineRule>(
            LegacyRateCardBaselineCodeLengthRule(3600),
            LegacyRateCardBaselineCodePrefixRule("Q"),
            LegacyRateCardBaselineNameLengthRule(1000),
            LegacyRateCardBaselineStopCountRangeRule(80, 3600),
        ),
    )
}
