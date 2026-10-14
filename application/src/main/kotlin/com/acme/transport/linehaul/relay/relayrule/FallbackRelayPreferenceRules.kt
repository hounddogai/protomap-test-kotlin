package com.acme.transport.linehaul.relay.relayrule

/** A rule a fallback relay preference must pass. Check returns its problems, or none. */
interface FallbackRelayPreferenceRule {
    fun check(item: FallbackRelayPreference): MutableList<String>
}

data class FallbackRelayPreferenceDiscountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRelayPreferenceRule {
    override fun check(item: FallbackRelayPreference): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayPreferenceCreatedAtRangeRule(var min: Int, var max: Int) : FallbackRelayPreferenceRule {
    override fun check(item: FallbackRelayPreference): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayPreferenceQuantityRangeRule(var min: Int, var max: Int) : FallbackRelayPreferenceRule {
    override fun check(item: FallbackRelayPreference): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback relay preference. */
data class FallbackRelayPreferenceValidator(var rules: MutableList<FallbackRelayPreferenceRule>) {
    fun validate(item: FallbackRelayPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRelayPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRelayPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRelayPreferenceValidator(): FallbackRelayPreferenceValidator {
    return FallbackRelayPreferenceValidator(
        mutableListOf<FallbackRelayPreferenceRule>(
            FallbackRelayPreferenceDiscountRangeRule(3.0, 4301.1),
            FallbackRelayPreferenceCreatedAtRangeRule(32, 32),
            FallbackRelayPreferenceQuantityRangeRule(1000, 86400),
        ),
    )
}
