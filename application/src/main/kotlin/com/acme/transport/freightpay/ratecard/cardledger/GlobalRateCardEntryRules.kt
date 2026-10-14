package com.acme.transport.freightpay.ratecard.cardledger

/** A rule a global rate card entry must pass. Check returns its problems, or none. */
interface GlobalRateCardEntryRule {
    fun check(item: GlobalRateCardEntry): MutableList<String>
}

data class GlobalRateCardEntryDiscountRangeRule(var min: Double, var max: Double) : GlobalRateCardEntryRule {
    override fun check(item: GlobalRateCardEntry): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class GlobalRateCardEntryCodeLengthRule(var maxLength: Int) : GlobalRateCardEntryRule {
    override fun check(item: GlobalRateCardEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardEntryRequiredCodeRule(var message: String) : GlobalRateCardEntryRule {
    override fun check(item: GlobalRateCardEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardEntryOdometerRangeRule(var min: Int, var max: Int) : GlobalRateCardEntryRule {
    override fun check(item: GlobalRateCardEntry): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rate card entry. */
data class GlobalRateCardEntryValidator(var rules: MutableList<GlobalRateCardEntryRule>) {
    fun validate(item: GlobalRateCardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRateCardEntryValidator(): GlobalRateCardEntryValidator {
    return GlobalRateCardEntryValidator(
        mutableListOf<GlobalRateCardEntryRule>(
            GlobalRateCardEntryDiscountRangeRule(6.5, 3491.7),
            GlobalRateCardEntryCodeLengthRule(1000),
            GlobalRateCardEntryRequiredCodeRule("global rate card entry needs this field"),
            GlobalRateCardEntryOdometerRangeRule(64, 120),
        ),
    )
}
