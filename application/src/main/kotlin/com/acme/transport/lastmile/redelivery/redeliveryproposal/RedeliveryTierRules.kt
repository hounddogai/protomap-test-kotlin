package com.acme.transport.lastmile.redelivery.redeliveryproposal

/** A rule a redelivery tier must pass. Check returns its problems, or none. */
interface RedeliveryTierRule {
    fun check(item: RedeliveryTier): MutableList<String>
}

data class RedeliveryTierCodeLengthRule(var maxLength: Int) : RedeliveryTierRule {
    override fun check(item: RedeliveryTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTierRequiredCodeRule(var message: String) : RedeliveryTierRule {
    override fun check(item: RedeliveryTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryTierWindowDaysRangeRule(var min: Int, var max: Int) : RedeliveryTierRule {
    override fun check(item: RedeliveryTier): MutableList<String> {
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

data class RedeliveryTierFuelLitersRangeRule(var min: Double, var max: Double) : RedeliveryTierRule {
    override fun check(item: RedeliveryTier): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery tier. */
data class RedeliveryTierValidator(var rules: MutableList<RedeliveryTierRule>) {
    fun validate(item: RedeliveryTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryTierValidator(): RedeliveryTierValidator {
    return RedeliveryTierValidator(
        mutableListOf<RedeliveryTierRule>(
            RedeliveryTierCodeLengthRule(32),
            RedeliveryTierRequiredCodeRule("redelivery tier needs a value"),
            RedeliveryTierWindowDaysRangeRule(255, 86400),
            RedeliveryTierFuelLitersRangeRule(7.5, 1045.6),
        ),
    )
}
