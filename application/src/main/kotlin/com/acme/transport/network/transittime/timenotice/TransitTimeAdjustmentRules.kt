package com.acme.transport.network.transittime.timenotice

/** A rule a transit time adjustment must pass. Check returns its problems, or none. */
interface TransitTimeAdjustmentRule {
    fun check(item: TransitTimeAdjustment): MutableList<String>
}

data class TransitTimeAdjustmentLimitRangeRule(var min: Int, var max: Int) : TransitTimeAdjustmentRule {
    override fun check(item: TransitTimeAdjustment): MutableList<String> {
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

data class TransitTimeAdjustmentTrailerIdLengthRule(var maxLength: Int) : TransitTimeAdjustmentRule {
    override fun check(item: TransitTimeAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAdjustmentLabelLengthRule(var maxLength: Int) : TransitTimeAdjustmentRule {
    override fun check(item: TransitTimeAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAdjustmentCodeLengthRule(var maxLength: Int) : TransitTimeAdjustmentRule {
    override fun check(item: TransitTimeAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAdjustmentCodePrefixRule(var prefix: String) : TransitTimeAdjustmentRule {
    override fun check(item: TransitTimeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time adjustment. */
data class TransitTimeAdjustmentValidator(var rules: MutableList<TransitTimeAdjustmentRule>) {
    fun validate(item: TransitTimeAdjustment): MutableList<String> {
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

    fun requireValid(item: TransitTimeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeAdjustmentValidator(): TransitTimeAdjustmentValidator {
    return TransitTimeAdjustmentValidator(
        mutableListOf<TransitTimeAdjustmentRule>(
            TransitTimeAdjustmentLimitRangeRule(32, 120),
            TransitTimeAdjustmentTrailerIdLengthRule(120),
            TransitTimeAdjustmentLabelLengthRule(120),
            TransitTimeAdjustmentCodeLengthRule(64),
            TransitTimeAdjustmentCodePrefixRule("ACM"),
        ),
    )
}
