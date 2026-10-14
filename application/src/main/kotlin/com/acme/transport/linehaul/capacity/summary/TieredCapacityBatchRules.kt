package com.acme.transport.linehaul.capacity.summary

/** A rule a tiered capacity batch must pass. Check returns its problems, or none. */
interface TieredCapacityBatchRule {
    fun check(item: TieredCapacityBatch): MutableList<String>
}

data class TieredCapacityBatchRequiredSegmentRule(var message: String) : TieredCapacityBatchRule {
    override fun check(item: TieredCapacityBatch): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityBatchRequiredExternalRefRule(var message: String) : TieredCapacityBatchRule {
    override fun check(item: TieredCapacityBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityBatchDistanceKmRangeRule(var min: Double, var max: Double) : TieredCapacityBatchRule {
    override fun check(item: TieredCapacityBatch): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityBatchMinimumRangeRule(var min: Double, var max: Double) : TieredCapacityBatchRule {
    override fun check(item: TieredCapacityBatch): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class TieredCapacityBatchCodePrefixRule(var prefix: String) : TieredCapacityBatchRule {
    override fun check(item: TieredCapacityBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered capacity batch. */
data class TieredCapacityBatchValidator(var rules: MutableList<TieredCapacityBatchRule>) {
    fun validate(item: TieredCapacityBatch): MutableList<String> {
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

    fun requireValid(item: TieredCapacityBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCapacityBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCapacityBatchValidator(): TieredCapacityBatchValidator {
    return TieredCapacityBatchValidator(
        mutableListOf<TieredCapacityBatchRule>(
            TieredCapacityBatchRequiredSegmentRule("tiered capacity batch needs this field"),
            TieredCapacityBatchRequiredExternalRefRule("tiered capacity batch needs this field"),
            TieredCapacityBatchDistanceKmRangeRule(6.0, 2150.3),
            TieredCapacityBatchMinimumRangeRule(3.9, 2548.4),
            TieredCapacityBatchCodePrefixRule("Q"),
        ),
    )
}
