package com.acme.transport.customs.clearance.clearancevariance

/** A rule a fallback clearance variance must pass. Check returns its problems, or none. */
interface FallbackClearanceVarianceRule {
    fun check(item: FallbackClearanceVariance): MutableList<String>
}

data class FallbackClearanceVarianceCodePrefixRule(var prefix: String) : FallbackClearanceVarianceRule {
    override fun check(item: FallbackClearanceVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearanceVarianceQuantityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackClearanceVarianceRule {
    override fun check(item: FallbackClearanceVariance): MutableList<String> {
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

data class FallbackClearanceVarianceRequiredCodeRule(var message: String) : FallbackClearanceVarianceRule {
    override fun check(item: FallbackClearanceVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback clearance variance. */
data class FallbackClearanceVarianceValidator(var rules: MutableList<FallbackClearanceVarianceRule>) {
    fun validate(item: FallbackClearanceVariance): MutableList<String> {
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

    fun requireValid(item: FallbackClearanceVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackClearanceVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackClearanceVarianceValidator(): FallbackClearanceVarianceValidator {
    return FallbackClearanceVarianceValidator(
        mutableListOf<FallbackClearanceVarianceRule>(
            FallbackClearanceVarianceCodePrefixRule("Q"),
            FallbackClearanceVarianceQuantityRangeRule(16, 32),
            FallbackClearanceVarianceRequiredCodeRule("fallback clearance variance needs a value"),
        ),
    )
}
