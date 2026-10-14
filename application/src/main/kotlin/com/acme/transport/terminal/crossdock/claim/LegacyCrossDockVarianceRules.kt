package com.acme.transport.terminal.crossdock.claim

/** A rule a legacy cross dock variance must pass. Check returns its problems, or none. */
interface LegacyCrossDockVarianceRule {
    fun check(item: LegacyCrossDockVariance): MutableList<String>
}

data class LegacyCrossDockVarianceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCrossDockVarianceRule {
    override fun check(item: LegacyCrossDockVariance): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockVarianceCodePrefixRule(var prefix: String) : LegacyCrossDockVarianceRule {
    override fun check(item: LegacyCrossDockVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockVarianceMinimumRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCrossDockVarianceRule {
    override fun check(item: LegacyCrossDockVariance): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class LegacyCrossDockVarianceRequiredContactEmailRule(var message: String) : LegacyCrossDockVarianceRule {
    override fun check(item: LegacyCrossDockVariance): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy cross dock variance. */
data class LegacyCrossDockVarianceValidator(var rules: MutableList<LegacyCrossDockVarianceRule>) {
    fun validate(item: LegacyCrossDockVariance): MutableList<String> {
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

    fun requireValid(item: LegacyCrossDockVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCrossDockVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCrossDockVarianceValidator(): LegacyCrossDockVarianceValidator {
    return LegacyCrossDockVarianceValidator(
        mutableListOf<LegacyCrossDockVarianceRule>(
            LegacyCrossDockVarianceDistanceKmRangeRule(4.0, 4534.3),
            LegacyCrossDockVarianceCodePrefixRule("Q"),
            LegacyCrossDockVarianceMinimumRangeRule(0.8, 4526.2),
            LegacyCrossDockVarianceRequiredContactEmailRule("legacy cross dock variance needs an entry"),
        ),
    )
}
