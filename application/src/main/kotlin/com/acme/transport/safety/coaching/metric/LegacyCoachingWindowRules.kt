package com.acme.transport.safety.coaching.metric

/** A rule a legacy coaching window must pass. Check returns its problems, or none. */
interface LegacyCoachingWindowRule {
    fun check(item: LegacyCoachingWindow): MutableList<String>
}

data class LegacyCoachingWindowCodePrefixRule(var prefix: String) : LegacyCoachingWindowRule {
    override fun check(item: LegacyCoachingWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingWindowLabelLengthRule(var maxLength: Int) : LegacyCoachingWindowRule {
    override fun check(item: LegacyCoachingWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingWindowVehicleIdLengthRule(var maxLength: Int) : LegacyCoachingWindowRule {
    override fun check(item: LegacyCoachingWindow): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingWindowFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCoachingWindowRule {
    override fun check(item: LegacyCoachingWindow): MutableList<String> {
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

/** Runs every rule of a legacy coaching window. */
data class LegacyCoachingWindowValidator(var rules: MutableList<LegacyCoachingWindowRule>) {
    fun validate(item: LegacyCoachingWindow): MutableList<String> {
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

    fun requireValid(item: LegacyCoachingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCoachingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCoachingWindowValidator(): LegacyCoachingWindowValidator {
    return LegacyCoachingWindowValidator(
        mutableListOf<LegacyCoachingWindowRule>(
            LegacyCoachingWindowCodePrefixRule("REF"),
            LegacyCoachingWindowLabelLengthRule(255),
            LegacyCoachingWindowVehicleIdLengthRule(86400),
            LegacyCoachingWindowFuelLitersRangeRule(9.8, 4489.0),
        ),
    )
}
