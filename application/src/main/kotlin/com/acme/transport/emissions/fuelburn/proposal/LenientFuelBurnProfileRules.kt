package com.acme.transport.emissions.fuelburn.proposal

/** A rule a lenient fuel burn profile must pass. Check returns its problems, or none. */
interface LenientFuelBurnProfileRule {
    fun check(item: LenientFuelBurnProfile): MutableList<String>
}

data class LenientFuelBurnProfileRequiredLocaleRule(var message: String) : LenientFuelBurnProfileRule {
    override fun check(item: LenientFuelBurnProfile): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnProfileStartsAtRangeRule(var min: Int, var max: Int) : LenientFuelBurnProfileRule {
    override fun check(item: LenientFuelBurnProfile): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class LenientFuelBurnProfileCodePrefixRule(var prefix: String) : LenientFuelBurnProfileRule {
    override fun check(item: LenientFuelBurnProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnProfileCodeLengthRule(var maxLength: Int) : LenientFuelBurnProfileRule {
    override fun check(item: LenientFuelBurnProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel burn profile. */
data class LenientFuelBurnProfileValidator(var rules: MutableList<LenientFuelBurnProfileRule>) {
    fun validate(item: LenientFuelBurnProfile): MutableList<String> {
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

    fun requireValid(item: LenientFuelBurnProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelBurnProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelBurnProfileValidator(): LenientFuelBurnProfileValidator {
    return LenientFuelBurnProfileValidator(
        mutableListOf<LenientFuelBurnProfileRule>(
            LenientFuelBurnProfileRequiredLocaleRule("lenient fuel burn profile needs a value"),
            LenientFuelBurnProfileStartsAtRangeRule(32, 86400),
            LenientFuelBurnProfileCodePrefixRule("Q"),
            LenientFuelBurnProfileCodeLengthRule(86400),
        ),
    )
}
