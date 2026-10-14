package com.acme.transport.freightpay.dispute.variance

/** A rule a fallback dispute checkpoint must pass. Check returns its problems, or none. */
interface FallbackDisputeCheckpointRule {
    fun check(item: FallbackDisputeCheckpoint): MutableList<String>
}

data class FallbackDisputeCheckpointCodeLengthRule(var maxLength: Int) : FallbackDisputeCheckpointRule {
    override fun check(item: FallbackDisputeCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeCheckpointTitleLengthRule(var maxLength: Int) : FallbackDisputeCheckpointRule {
    override fun check(item: FallbackDisputeCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeCheckpointCodePrefixRule(var prefix: String) : FallbackDisputeCheckpointRule {
    override fun check(item: FallbackDisputeCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeCheckpointCostRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDisputeCheckpointRule {
    override fun check(item: FallbackDisputeCheckpoint): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback dispute checkpoint. */
data class FallbackDisputeCheckpointValidator(var rules: MutableList<FallbackDisputeCheckpointRule>) {
    fun validate(item: FallbackDisputeCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDisputeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputeCheckpointValidator(): FallbackDisputeCheckpointValidator {
    return FallbackDisputeCheckpointValidator(
        mutableListOf<FallbackDisputeCheckpointRule>(
            FallbackDisputeCheckpointCodeLengthRule(3600),
            FallbackDisputeCheckpointTitleLengthRule(16),
            FallbackDisputeCheckpointCodePrefixRule("OPS"),
            FallbackDisputeCheckpointCostRangeRule(1.8, 825.3),
        ),
    )
}
