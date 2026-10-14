package com.acme.transport.freightpay.dispute.slot

/** A rule a fallback dispute plan must pass. Check returns its problems, or none. */
interface FallbackDisputePlanRule {
    fun check(item: FallbackDisputePlan): MutableList<String>
}

data class FallbackDisputePlanExternalRefLengthRule(var maxLength: Int) : FallbackDisputePlanRule {
    override fun check(item: FallbackDisputePlan): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputePlanWindowDaysRangeRule(var min: Int, var max: Int) : FallbackDisputePlanRule {
    override fun check(item: FallbackDisputePlan): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputePlanRequiredLabelRule(var message: String) : FallbackDisputePlanRule {
    override fun check(item: FallbackDisputePlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputePlanEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackDisputePlanRule {
    override fun check(item: FallbackDisputePlan): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputePlanMaximumRangeRule(var min: Double, var max: Double) : FallbackDisputePlanRule {
    override fun check(item: FallbackDisputePlan): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback dispute plan. */
data class FallbackDisputePlanValidator(var rules: MutableList<FallbackDisputePlanRule>) {
    fun validate(item: FallbackDisputePlan): MutableList<String> {
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

    fun requireValid(item: FallbackDisputePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputePlanValidator(): FallbackDisputePlanValidator {
    return FallbackDisputePlanValidator(
        mutableListOf<FallbackDisputePlanRule>(
            FallbackDisputePlanExternalRefLengthRule(32),
            FallbackDisputePlanWindowDaysRangeRule(120, 1000),
            FallbackDisputePlanRequiredLabelRule("fallback dispute plan needs an entry"),
            FallbackDisputePlanEtaMinutesRangeRule(80, 120),
            FallbackDisputePlanMaximumRangeRule(8.2, 2728.0),
        ),
    )
}
