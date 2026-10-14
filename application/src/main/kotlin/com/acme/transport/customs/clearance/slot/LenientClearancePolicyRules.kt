package com.acme.transport.customs.clearance.slot

/** A rule a lenient clearance policy must pass. Check returns its problems, or none. */
interface LenientClearancePolicyRule {
    fun check(item: LenientClearancePolicy): MutableList<String>
}

data class LenientClearancePolicyRequiredCodeRule(var message: String) : LenientClearancePolicyRule {
    override fun check(item: LenientClearancePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearancePolicyCurrencyLengthRule(var maxLength: Int) : LenientClearancePolicyRule {
    override fun check(item: LenientClearancePolicy): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearancePolicyDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientClearancePolicyRule {
    override fun check(item: LenientClearancePolicy): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LenientClearancePolicyCodePrefixRule(var prefix: String) : LenientClearancePolicyRule {
    override fun check(item: LenientClearancePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearancePolicyVersionRangeRule(var min: Int, var max: Int) : LenientClearancePolicyRule {
    override fun check(item: LenientClearancePolicy): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient clearance policy. */
data class LenientClearancePolicyValidator(var rules: MutableList<LenientClearancePolicyRule>) {
    fun validate(item: LenientClearancePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientClearancePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientClearancePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientClearancePolicyValidator(): LenientClearancePolicyValidator {
    return LenientClearancePolicyValidator(
        mutableListOf<LenientClearancePolicyRule>(
            LenientClearancePolicyRequiredCodeRule("lenient clearance policy needs an entry"),
            LenientClearancePolicyCurrencyLengthRule(3600),
            LenientClearancePolicyDiscountRangeRule(9.0, 1842.0),
            LenientClearancePolicyCodePrefixRule("ACM"),
            LenientClearancePolicyVersionRangeRule(16, 16),
        ),
    )
}
