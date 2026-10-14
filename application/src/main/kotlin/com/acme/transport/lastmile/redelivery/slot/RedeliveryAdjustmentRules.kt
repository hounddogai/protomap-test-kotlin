package com.acme.transport.lastmile.redelivery.slot

/** A rule a redelivery adjustment must pass. Check returns its problems, or none. */
interface RedeliveryAdjustmentRule {
    fun check(item: RedeliveryAdjustment): MutableList<String>
}

data class RedeliveryAdjustmentTimezoneLengthRule(var maxLength: Int) : RedeliveryAdjustmentRule {
    override fun check(item: RedeliveryAdjustment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAdjustmentRequiredCodeRule(var message: String) : RedeliveryAdjustmentRule {
    override fun check(item: RedeliveryAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAdjustmentAmountRangeRule(var min: Double, var max: Double) : RedeliveryAdjustmentRule {
    override fun check(item: RedeliveryAdjustment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RedeliveryAdjustmentCodePrefixRule(var prefix: String) : RedeliveryAdjustmentRule {
    override fun check(item: RedeliveryAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAdjustmentCodeLengthRule(var maxLength: Int) : RedeliveryAdjustmentRule {
    override fun check(item: RedeliveryAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery adjustment. */
data class RedeliveryAdjustmentValidator(var rules: MutableList<RedeliveryAdjustmentRule>) {
    fun validate(item: RedeliveryAdjustment): MutableList<String> {
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

    fun requireValid(item: RedeliveryAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryAdjustmentValidator(): RedeliveryAdjustmentValidator {
    return RedeliveryAdjustmentValidator(
        mutableListOf<RedeliveryAdjustmentRule>(
            RedeliveryAdjustmentTimezoneLengthRule(120),
            RedeliveryAdjustmentRequiredCodeRule("redelivery adjustment needs an entry"),
            RedeliveryAdjustmentAmountRangeRule(9.7, 4694.9),
            RedeliveryAdjustmentCodePrefixRule("OPS"),
            RedeliveryAdjustmentCodeLengthRule(32),
        ),
    )
}
