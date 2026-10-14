package com.acme.transport.emissions.routeoffset.threshold

/** A rule a batched route offset target must pass. Check returns its problems, or none. */
interface BatchedRouteOffsetTargetRule {
    fun check(item: BatchedRouteOffsetTarget): MutableList<String>
}

data class BatchedRouteOffsetTargetCodePrefixRule(var prefix: String) : BatchedRouteOffsetTargetRule {
    override fun check(item: BatchedRouteOffsetTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetTargetAmountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRouteOffsetTargetRule {
    override fun check(item: BatchedRouteOffsetTarget): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetTargetMaximumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRouteOffsetTargetRule {
    override fun check(item: BatchedRouteOffsetTarget): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedRouteOffsetTargetCodeLengthRule(var maxLength: Int) : BatchedRouteOffsetTargetRule {
    override fun check(item: BatchedRouteOffsetTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRouteOffsetTargetRequiredLabelRule(var message: String) : BatchedRouteOffsetTargetRule {
    override fun check(item: BatchedRouteOffsetTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched route offset target. */
data class BatchedRouteOffsetTargetValidator(var rules: MutableList<BatchedRouteOffsetTargetRule>) {
    fun validate(item: BatchedRouteOffsetTarget): MutableList<String> {
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

    fun requireValid(item: BatchedRouteOffsetTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRouteOffsetTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRouteOffsetTargetValidator(): BatchedRouteOffsetTargetValidator {
    return BatchedRouteOffsetTargetValidator(
        mutableListOf<BatchedRouteOffsetTargetRule>(
            BatchedRouteOffsetTargetCodePrefixRule("OPS"),
            BatchedRouteOffsetTargetAmountRangeRule(6.3, 4238.6),
            BatchedRouteOffsetTargetMaximumRangeRule(4.3, 2321.3),
            BatchedRouteOffsetTargetCodeLengthRule(80),
            BatchedRouteOffsetTargetRequiredLabelRule("batched route offset target needs an entry"),
        ),
    )
}
