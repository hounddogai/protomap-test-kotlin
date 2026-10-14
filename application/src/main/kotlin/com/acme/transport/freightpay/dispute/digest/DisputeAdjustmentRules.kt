package com.acme.transport.freightpay.dispute.digest

/** A rule a dispute adjustment must pass. Check returns its problems, or none. */
interface DisputeAdjustmentRule {
    fun check(item: DisputeAdjustment): MutableList<String>
}

data class DisputeAdjustmentCeilingRangeRule(var min: Double, var max: Double) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAdjustmentRequiredCodeRule(var message: String) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeAdjustmentRateRangeRule(var min: Double, var max: Double) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DisputeAdjustmentCodeLengthRule(var maxLength: Int) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAdjustmentCodePrefixRule(var prefix: String) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAdjustmentGraceDaysRangeRule(var min: Int, var max: Int) : DisputeAdjustmentRule {
    override fun check(item: DisputeAdjustment): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute adjustment. */
data class DisputeAdjustmentValidator(var rules: MutableList<DisputeAdjustmentRule>) {
    fun validate(item: DisputeAdjustment): MutableList<String> {
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

    fun requireValid(item: DisputeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeAdjustmentValidator(): DisputeAdjustmentValidator {
    return DisputeAdjustmentValidator(
        mutableListOf<DisputeAdjustmentRule>(
            DisputeAdjustmentCeilingRangeRule(6.4, 2925.0),
            DisputeAdjustmentRequiredCodeRule("dispute adjustment needs a value"),
            DisputeAdjustmentRateRangeRule(1.7, 208.8),
            DisputeAdjustmentCodeLengthRule(255),
            DisputeAdjustmentCodePrefixRule("X"),
            DisputeAdjustmentGraceDaysRangeRule(16, 80),
        ),
    )
}
