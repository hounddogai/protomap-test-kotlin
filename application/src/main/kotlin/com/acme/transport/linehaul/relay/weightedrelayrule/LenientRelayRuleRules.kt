package com.acme.transport.linehaul.relay.weightedrelayrule

/** A rule a lenient relay rule must pass. Check returns its problems, or none. */
interface LenientRelayRuleRule {
    fun check(item: LenientRelayRule): MutableList<String>
}

data class LenientRelayRuleRequiredCountryRule(var message: String) : LenientRelayRuleRule {
    override fun check(item: LenientRelayRule): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayRuleCeilingRangeRule(var min: Double, var max: Double) : LenientRelayRuleRule {
    override fun check(item: LenientRelayRule): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayRuleCodePrefixRule(var prefix: String) : LenientRelayRuleRule {
    override fun check(item: LenientRelayRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientRelayRuleOdometerRangeRule(var min: Int, var max: Int) : LenientRelayRuleRule {
    override fun check(item: LenientRelayRule): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient relay rule. */
data class LenientRelayRuleValidator(var rules: MutableList<LenientRelayRuleRule>) {
    fun validate(item: LenientRelayRule): MutableList<String> {
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

    fun requireValid(item: LenientRelayRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRelayRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRelayRuleValidator(): LenientRelayRuleValidator {
    return LenientRelayRuleValidator(
        mutableListOf<LenientRelayRuleRule>(
            LenientRelayRuleRequiredCountryRule("lenient relay rule needs this field"),
            LenientRelayRuleCeilingRangeRule(6.1, 4284.5),
            LenientRelayRuleCodePrefixRule("ACM"),
            LenientRelayRuleOdometerRangeRule(16, 255),
        ),
    )
}
