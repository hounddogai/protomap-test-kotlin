package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.normalize

/** A rule a rolling near miss adjustment must pass. Check returns its problems, or none. */
interface RollingNearMissAdjustmentRule {
    fun check(item: RollingNearMissAdjustment): MutableList<String>
}

data class RollingNearMissAdjustmentRequiredExternalRefRule(
    var message: String,
) : RollingNearMissAdjustmentRule {
    override fun check(item: RollingNearMissAdjustment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissAdjustmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingNearMissAdjustmentRule {
    override fun check(item: RollingNearMissAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissAdjustmentCodePrefixRule(var prefix: String) : RollingNearMissAdjustmentRule {
    override fun check(item: RollingNearMissAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissAdjustmentRequiredCodeRule(var message: String) : RollingNearMissAdjustmentRule {
    override fun check(item: RollingNearMissAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissAdjustmentBudgetRangeRule(
    var min: Double,
    var max: Double,
) : RollingNearMissAdjustmentRule {
    override fun check(item: RollingNearMissAdjustment): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling near miss adjustment. */
data class RollingNearMissAdjustmentValidator(var rules: MutableList<RollingNearMissAdjustmentRule>) {
    fun validate(item: RollingNearMissAdjustment): MutableList<String> {
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

    fun requireValid(item: RollingNearMissAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNearMissAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNearMissAdjustmentValidator(): RollingNearMissAdjustmentValidator {
    return RollingNearMissAdjustmentValidator(
        mutableListOf<RollingNearMissAdjustmentRule>(
            RollingNearMissAdjustmentRequiredExternalRefRule("rolling near miss adjustment needs this field"),
            RollingNearMissAdjustmentAllowedCodeRule(mutableListOf<String>("global", "fallback", "scheduled")),
            RollingNearMissAdjustmentCodePrefixRule("OPS"),
            RollingNearMissAdjustmentRequiredCodeRule("rolling near miss adjustment needs a value"),
            RollingNearMissAdjustmentBudgetRangeRule(0.0, 1595.9),
        ),
    )
}
