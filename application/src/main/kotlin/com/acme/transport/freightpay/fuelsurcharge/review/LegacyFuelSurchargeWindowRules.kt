package com.acme.transport.freightpay.fuelsurcharge.review

/** A rule a legacy fuel surcharge window must pass. Check returns its problems, or none. */
interface LegacyFuelSurchargeWindowRule {
    fun check(item: LegacyFuelSurchargeWindow): MutableList<String>
}

data class LegacyFuelSurchargeWindowRequiredNameRule(var message: String) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeWindowStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
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

data class LegacyFuelSurchargeWindowRequiredCodeRule(var message: String) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeWindowRetriesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeWindowCodePrefixRule(var prefix: String) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelSurchargeWindowFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyFuelSurchargeWindowRule {
    override fun check(item: LegacyFuelSurchargeWindow): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy fuel surcharge window. */
data class LegacyFuelSurchargeWindowValidator(var rules: MutableList<LegacyFuelSurchargeWindowRule>) {
    fun validate(item: LegacyFuelSurchargeWindow): MutableList<String> {
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

    fun requireValid(item: LegacyFuelSurchargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFuelSurchargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFuelSurchargeWindowValidator(): LegacyFuelSurchargeWindowValidator {
    return LegacyFuelSurchargeWindowValidator(
        mutableListOf<LegacyFuelSurchargeWindowRule>(
            LegacyFuelSurchargeWindowRequiredNameRule("legacy fuel surcharge window needs a value"),
            LegacyFuelSurchargeWindowStartsAtRangeRule(32, 1000),
            LegacyFuelSurchargeWindowRequiredCodeRule("legacy fuel surcharge window needs this field"),
            LegacyFuelSurchargeWindowRetriesRangeRule(80, 1000),
            LegacyFuelSurchargeWindowCodePrefixRule("X"),
            LegacyFuelSurchargeWindowFuelLitersRangeRule(8.0, 4713.9),
        ),
    )
}
