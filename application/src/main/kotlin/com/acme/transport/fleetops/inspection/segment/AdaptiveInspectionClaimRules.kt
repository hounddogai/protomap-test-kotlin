package com.acme.transport.fleetops.inspection.segment

/** A rule a adaptive inspection claim must pass. Check returns its problems, or none. */
interface AdaptiveInspectionClaimRule {
    fun check(item: AdaptiveInspectionClaim): MutableList<String>
}

data class AdaptiveInspectionClaimCodePrefixRule(var prefix: String) : AdaptiveInspectionClaimRule {
    override fun check(item: AdaptiveInspectionClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionClaimWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionClaimRule {
    override fun check(item: AdaptiveInspectionClaim): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionClaimRequiredCodeRule(var message: String) : AdaptiveInspectionClaimRule {
    override fun check(item: AdaptiveInspectionClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection claim. */
data class AdaptiveInspectionClaimValidator(var rules: MutableList<AdaptiveInspectionClaimRule>) {
    fun validate(item: AdaptiveInspectionClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionClaimValidator(): AdaptiveInspectionClaimValidator {
    return AdaptiveInspectionClaimValidator(
        mutableListOf<AdaptiveInspectionClaimRule>(
            AdaptiveInspectionClaimCodePrefixRule("Q"),
            AdaptiveInspectionClaimWindowDaysRangeRule(64, 64),
            AdaptiveInspectionClaimRequiredCodeRule("adaptive inspection claim needs an entry"),
        ),
    )
}
