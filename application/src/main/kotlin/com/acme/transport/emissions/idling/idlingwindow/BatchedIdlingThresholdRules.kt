package com.acme.transport.emissions.idling.idlingwindow

/** A rule a batched idling threshold must pass. Check returns its problems, or none. */
interface BatchedIdlingThresholdRule {
    fun check(item: BatchedIdlingThreshold): MutableList<String>
}

data class BatchedIdlingThresholdFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedIdlingThresholdRule {
    override fun check(item: BatchedIdlingThreshold): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingThresholdRequiredNameRule(var message: String) : BatchedIdlingThresholdRule {
    override fun check(item: BatchedIdlingThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingThresholdCodePrefixRule(var prefix: String) : BatchedIdlingThresholdRule {
    override fun check(item: BatchedIdlingThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingThresholdLaneLengthRule(var maxLength: Int) : BatchedIdlingThresholdRule {
    override fun check(item: BatchedIdlingThreshold): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched idling threshold. */
data class BatchedIdlingThresholdValidator(var rules: MutableList<BatchedIdlingThresholdRule>) {
    fun validate(item: BatchedIdlingThreshold): MutableList<String> {
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

    fun requireValid(item: BatchedIdlingThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIdlingThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIdlingThresholdValidator(): BatchedIdlingThresholdValidator {
    return BatchedIdlingThresholdValidator(
        mutableListOf<BatchedIdlingThresholdRule>(
            BatchedIdlingThresholdFuelLitersRangeRule(7.1, 3298.2),
            BatchedIdlingThresholdRequiredNameRule("batched idling threshold needs a value"),
            BatchedIdlingThresholdCodePrefixRule("OPS"),
            BatchedIdlingThresholdLaneLengthRule(64),
        ),
    )
}
