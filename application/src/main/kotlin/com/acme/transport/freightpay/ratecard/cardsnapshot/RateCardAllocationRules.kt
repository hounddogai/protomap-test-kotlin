package com.acme.transport.freightpay.ratecard.cardsnapshot

/** A rule a rate card allocation must pass. Check returns its problems, or none. */
interface RateCardAllocationRule {
    fun check(item: RateCardAllocation): MutableList<String>
}

data class RateCardAllocationGraceDaysRangeRule(var min: Int, var max: Int) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RateCardAllocationRequiredTitleRule(var message: String) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAllocationCodePrefixRule(var prefix: String) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAllocationRequiredCodeRule(var message: String) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAllocationTitleLengthRule(var maxLength: Int) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAllocationRequiredShiftCodeRule(var message: String) : RateCardAllocationRule {
    override fun check(item: RateCardAllocation): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card allocation. */
data class RateCardAllocationValidator(var rules: MutableList<RateCardAllocationRule>) {
    fun validate(item: RateCardAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardAllocationValidator(): RateCardAllocationValidator {
    return RateCardAllocationValidator(
        mutableListOf<RateCardAllocationRule>(
            RateCardAllocationGraceDaysRangeRule(32, 1000),
            RateCardAllocationRequiredTitleRule("rate card allocation needs this field"),
            RateCardAllocationCodePrefixRule("Q"),
            RateCardAllocationRequiredCodeRule("rate card allocation needs a value"),
            RateCardAllocationTitleLengthRule(255),
            RateCardAllocationRequiredShiftCodeRule("rate card allocation needs an entry"),
        ),
    )
}
