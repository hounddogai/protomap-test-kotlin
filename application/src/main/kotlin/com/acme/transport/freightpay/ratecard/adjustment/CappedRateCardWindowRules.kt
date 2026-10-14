package com.acme.transport.freightpay.ratecard.adjustment

/** A rule a capped rate card window must pass. Check returns its problems, or none. */
interface CappedRateCardWindowRule {
    fun check(item: CappedRateCardWindow): MutableList<String>
}

data class CappedRateCardWindowTimezoneLengthRule(var maxLength: Int) : CappedRateCardWindowRule {
    override fun check(item: CappedRateCardWindow): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardWindowCodePrefixRule(var prefix: String) : CappedRateCardWindowRule {
    override fun check(item: CappedRateCardWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardWindowCapacityRangeRule(var min: Int, var max: Int) : CappedRateCardWindowRule {
    override fun check(item: CappedRateCardWindow): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRateCardWindowPalletsRangeRule(var min: Int, var max: Int) : CappedRateCardWindowRule {
    override fun check(item: CappedRateCardWindow): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rate card window. */
data class CappedRateCardWindowValidator(var rules: MutableList<CappedRateCardWindowRule>) {
    fun validate(item: CappedRateCardWindow): MutableList<String> {
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

    fun requireValid(item: CappedRateCardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRateCardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRateCardWindowValidator(): CappedRateCardWindowValidator {
    return CappedRateCardWindowValidator(
        mutableListOf<CappedRateCardWindowRule>(
            CappedRateCardWindowTimezoneLengthRule(1000),
            CappedRateCardWindowCodePrefixRule("REF"),
            CappedRateCardWindowCapacityRangeRule(255, 1000),
            CappedRateCardWindowPalletsRangeRule(80, 1000),
        ),
    )
}
