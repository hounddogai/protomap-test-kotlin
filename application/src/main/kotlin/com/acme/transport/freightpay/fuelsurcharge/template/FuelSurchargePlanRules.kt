package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge plan must pass. Check returns its problems, or none. */
interface FuelSurchargePlanRule {
    fun check(item: FuelSurchargePlan): MutableList<String>
}

data class FuelSurchargePlanExternalRefLengthRule(var maxLength: Int) : FuelSurchargePlanRule {
    override fun check(item: FuelSurchargePlan): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePlanAllowedExternalRefRule(var allowed: MutableList<String>) : FuelSurchargePlanRule {
    override fun check(item: FuelSurchargePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePlanDurationMinutesRangeRule(var min: Int, var max: Int) : FuelSurchargePlanRule {
    override fun check(item: FuelSurchargePlan): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargePlanCreatedAtRangeRule(var min: Int, var max: Int) : FuelSurchargePlanRule {
    override fun check(item: FuelSurchargePlan): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePlanCodePrefixRule(var prefix: String) : FuelSurchargePlanRule {
    override fun check(item: FuelSurchargePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge plan. */
data class FuelSurchargePlanValidator(var rules: MutableList<FuelSurchargePlanRule>) {
    fun validate(item: FuelSurchargePlan): MutableList<String> {
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

    fun requireValid(item: FuelSurchargePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargePlanValidator(): FuelSurchargePlanValidator {
    return FuelSurchargePlanValidator(
        mutableListOf<FuelSurchargePlanRule>(
            FuelSurchargePlanExternalRefLengthRule(3600),
            FuelSurchargePlanAllowedExternalRefRule(mutableListOf<String>()),
            FuelSurchargePlanDurationMinutesRangeRule(255, 86400),
            FuelSurchargePlanCreatedAtRangeRule(32, 120),
            FuelSurchargePlanCodePrefixRule("ACM"),
        ),
    )
}
