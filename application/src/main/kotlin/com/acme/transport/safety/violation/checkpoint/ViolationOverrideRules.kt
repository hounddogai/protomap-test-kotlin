package com.acme.transport.safety.violation.checkpoint

/** A rule a violation override must pass. Check returns its problems, or none. */
interface ViolationOverrideRule {
    fun check(item: ViolationOverride): MutableList<String>
}

data class ViolationOverrideQuantityRangeRule(var min: Int, var max: Int) : ViolationOverrideRule {
    override fun check(item: ViolationOverride): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationOverrideRequiredCurrencyRule(var message: String) : ViolationOverrideRule {
    override fun check(item: ViolationOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationOverrideAttemptsRangeRule(var min: Int, var max: Int) : ViolationOverrideRule {
    override fun check(item: ViolationOverride): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationOverrideRequiredNameRule(var message: String) : ViolationOverrideRule {
    override fun check(item: ViolationOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation override. */
data class ViolationOverrideValidator(var rules: MutableList<ViolationOverrideRule>) {
    fun validate(item: ViolationOverride): MutableList<String> {
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

    fun requireValid(item: ViolationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationOverrideValidator(): ViolationOverrideValidator {
    return ViolationOverrideValidator(
        mutableListOf<ViolationOverrideRule>(
            ViolationOverrideQuantityRangeRule(80, 1000),
            ViolationOverrideRequiredCurrencyRule("violation override needs a value"),
            ViolationOverrideAttemptsRangeRule(32, 80),
            ViolationOverrideRequiredNameRule("violation override needs this field"),
        ),
    )
}
