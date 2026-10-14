package com.acme.transport.network.servicearea.manualareathreshold

/** A rule a lenient service area window must pass. Check returns its problems, or none. */
interface LenientServiceAreaWindowRule {
    fun check(item: LenientServiceAreaWindow): MutableList<String>
}

data class LenientServiceAreaWindowCodePrefixRule(var prefix: String) : LenientServiceAreaWindowRule {
    override fun check(item: LenientServiceAreaWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaWindowRequiredChecksumRule(var message: String) : LenientServiceAreaWindowRule {
    override fun check(item: LenientServiceAreaWindow): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaWindowAmountRangeRule(
    var min: Double,
    var max: Double,
) : LenientServiceAreaWindowRule {
    override fun check(item: LenientServiceAreaWindow): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient service area window. */
data class LenientServiceAreaWindowValidator(var rules: MutableList<LenientServiceAreaWindowRule>) {
    fun validate(item: LenientServiceAreaWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServiceAreaWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaWindowValidator(): LenientServiceAreaWindowValidator {
    return LenientServiceAreaWindowValidator(
        mutableListOf<LenientServiceAreaWindowRule>(
            LenientServiceAreaWindowCodePrefixRule("REF"),
            LenientServiceAreaWindowRequiredChecksumRule("lenient service area window needs an entry"),
            LenientServiceAreaWindowAmountRangeRule(8.2, 1810.7),
        ),
    )
}
