package com.acme.transport.freightpay.ratecard.group

/** A rule a rate card group must pass. Check returns its problems, or none. */
interface RateCardGroupRule {
    fun check(item: RateCardGroup): MutableList<String>
}

data class RateCardGroupWindowDaysRangeRule(var min: Int, var max: Int) : RateCardGroupRule {
    override fun check(item: RateCardGroup): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RateCardGroupRatioRangeRule(var min: Double, var max: Double) : RateCardGroupRule {
    override fun check(item: RateCardGroup): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RateCardGroupCodePrefixRule(var prefix: String) : RateCardGroupRule {
    override fun check(item: RateCardGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardGroupCarrierLengthRule(var maxLength: Int) : RateCardGroupRule {
    override fun check(item: RateCardGroup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardGroupRequiredCarrierRule(var message: String) : RateCardGroupRule {
    override fun check(item: RateCardGroup): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card group. */
data class RateCardGroupValidator(var rules: MutableList<RateCardGroupRule>) {
    fun validate(item: RateCardGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardGroupValidator(): RateCardGroupValidator {
    return RateCardGroupValidator(
        mutableListOf<RateCardGroupRule>(
            RateCardGroupWindowDaysRangeRule(255, 3600),
            RateCardGroupRatioRangeRule(0.7, 1386.2),
            RateCardGroupCodePrefixRule("X"),
            RateCardGroupCarrierLengthRule(1000),
            RateCardGroupRequiredCarrierRule("rate card group needs a value"),
        ),
    )
}
