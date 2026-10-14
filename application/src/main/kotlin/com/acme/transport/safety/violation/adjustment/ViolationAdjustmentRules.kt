package com.acme.transport.safety.violation.adjustment

/** A rule a violation adjustment must pass. Check returns its problems, or none. */
interface ViolationAdjustmentRule {
    fun check(item: ViolationAdjustment): MutableList<String>
}

data class ViolationAdjustmentLimitRangeRule(var min: Int, var max: Int) : ViolationAdjustmentRule {
    override fun check(item: ViolationAdjustment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class ViolationAdjustmentRequiredNameRule(var message: String) : ViolationAdjustmentRule {
    override fun check(item: ViolationAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationAdjustmentCodeLengthRule(var maxLength: Int) : ViolationAdjustmentRule {
    override fun check(item: ViolationAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation adjustment. */
data class ViolationAdjustmentValidator(var rules: MutableList<ViolationAdjustmentRule>) {
    fun validate(item: ViolationAdjustment): MutableList<String> {
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

    fun requireValid(item: ViolationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationAdjustmentValidator(): ViolationAdjustmentValidator {
    return ViolationAdjustmentValidator(
        mutableListOf<ViolationAdjustmentRule>(
            ViolationAdjustmentLimitRangeRule(120, 86400),
            ViolationAdjustmentRequiredNameRule("violation adjustment needs this field"),
            ViolationAdjustmentCodeLengthRule(32),
        ),
    )
}
