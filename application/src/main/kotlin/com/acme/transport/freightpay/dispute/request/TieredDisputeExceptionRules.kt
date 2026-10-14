package com.acme.transport.freightpay.dispute.request

/** A rule a tiered dispute exception must pass. Check returns its problems, or none. */
interface TieredDisputeExceptionRule {
    fun check(item: TieredDisputeException): MutableList<String>
}

data class TieredDisputeExceptionCodePrefixRule(var prefix: String) : TieredDisputeExceptionRule {
    override fun check(item: TieredDisputeException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeExceptionCostRangeRule(var min: Double, var max: Double) : TieredDisputeExceptionRule {
    override fun check(item: TieredDisputeException): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeExceptionDockLengthRule(var maxLength: Int) : TieredDisputeExceptionRule {
    override fun check(item: TieredDisputeException): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dispute exception. */
data class TieredDisputeExceptionValidator(var rules: MutableList<TieredDisputeExceptionRule>) {
    fun validate(item: TieredDisputeException): MutableList<String> {
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

    fun requireValid(item: TieredDisputeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDisputeException>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDisputeExceptionValidator(): TieredDisputeExceptionValidator {
    return TieredDisputeExceptionValidator(
        mutableListOf<TieredDisputeExceptionRule>(
            TieredDisputeExceptionCodePrefixRule("ACM"),
            TieredDisputeExceptionCostRangeRule(2.7, 4198.5),
            TieredDisputeExceptionDockLengthRule(255),
        ),
    )
}
