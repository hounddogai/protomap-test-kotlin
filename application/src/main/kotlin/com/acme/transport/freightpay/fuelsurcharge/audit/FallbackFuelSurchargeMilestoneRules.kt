package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize

/** A rule a fallback fuel surcharge milestone must pass. Check returns its problems, or none. */
interface FallbackFuelSurchargeMilestoneRule {
    fun check(item: FallbackFuelSurchargeMilestone): MutableList<String>
}

data class FallbackFuelSurchargeMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackFuelSurchargeMilestoneRule {
    override fun check(item: FallbackFuelSurchargeMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeMilestoneThresholdRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFuelSurchargeMilestoneRule {
    override fun check(item: FallbackFuelSurchargeMilestone): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class FallbackFuelSurchargeMilestoneRequiredPhoneRule(
    var message: String,
) : FallbackFuelSurchargeMilestoneRule {
    override fun check(item: FallbackFuelSurchargeMilestone): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback fuel surcharge milestone. */
data class FallbackFuelSurchargeMilestoneValidator(var rules: MutableList<FallbackFuelSurchargeMilestoneRule>) {
    fun validate(item: FallbackFuelSurchargeMilestone): MutableList<String> {
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

    fun requireValid(item: FallbackFuelSurchargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFuelSurchargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFuelSurchargeMilestoneValidator(): FallbackFuelSurchargeMilestoneValidator {
    return FallbackFuelSurchargeMilestoneValidator(
        mutableListOf<FallbackFuelSurchargeMilestoneRule>(
            FallbackFuelSurchargeMilestoneAllowedCodeRule(
                mutableListOf<String>("seasonal", "lenient", "strict"),
            ),
            FallbackFuelSurchargeMilestoneThresholdRangeRule(8.8, 91.7),
            FallbackFuelSurchargeMilestoneRequiredPhoneRule("fallback fuel surcharge milestone needs a value"),
        ),
    )
}
