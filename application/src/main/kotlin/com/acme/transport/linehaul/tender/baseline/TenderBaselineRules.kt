package com.acme.transport.linehaul.tender.baseline

/** A rule a tender baseline must pass. Check returns its problems, or none. */
interface TenderBaselineRule {
    fun check(item: TenderBaseline): MutableList<String>
}

data class TenderBaselineRequiredCodeRule(var message: String) : TenderBaselineRule {
    override fun check(item: TenderBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderBaselineCodePrefixRule(var prefix: String) : TenderBaselineRule {
    override fun check(item: TenderBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderBaselineMarginRangeRule(var min: Double, var max: Double) : TenderBaselineRule {
    override fun check(item: TenderBaseline): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderBaselineEndsAtRangeRule(var min: Int, var max: Int) : TenderBaselineRule {
    override fun check(item: TenderBaseline): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender baseline. */
data class TenderBaselineValidator(var rules: MutableList<TenderBaselineRule>) {
    fun validate(item: TenderBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderBaselineValidator(): TenderBaselineValidator {
    return TenderBaselineValidator(
        mutableListOf<TenderBaselineRule>(
            TenderBaselineRequiredCodeRule("tender baseline needs a value"),
            TenderBaselineCodePrefixRule("ACM"),
            TenderBaselineMarginRangeRule(9.5, 1891.6),
            TenderBaselineEndsAtRangeRule(16, 1000),
        ),
    )
}
