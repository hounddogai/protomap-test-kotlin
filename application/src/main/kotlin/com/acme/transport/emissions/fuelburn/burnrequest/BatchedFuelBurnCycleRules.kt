package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.normalize

/** A rule a batched fuel burn cycle must pass. Check returns its problems, or none. */
interface BatchedFuelBurnCycleRule {
    fun check(item: BatchedFuelBurnCycle): MutableList<String>
}

data class BatchedFuelBurnCycleDurationMinutesRangeRule(var min: Int, var max: Int) : BatchedFuelBurnCycleRule {
    override fun check(item: BatchedFuelBurnCycle): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class BatchedFuelBurnCycleCodePrefixRule(var prefix: String) : BatchedFuelBurnCycleRule {
    override fun check(item: BatchedFuelBurnCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnCycleAllowedTitleRule(var allowed: MutableList<String>) : BatchedFuelBurnCycleRule {
    override fun check(item: BatchedFuelBurnCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnCycleCodeLengthRule(var maxLength: Int) : BatchedFuelBurnCycleRule {
    override fun check(item: BatchedFuelBurnCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel burn cycle. */
data class BatchedFuelBurnCycleValidator(var rules: MutableList<BatchedFuelBurnCycleRule>) {
    fun validate(item: BatchedFuelBurnCycle): MutableList<String> {
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

    fun requireValid(item: BatchedFuelBurnCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelBurnCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelBurnCycleValidator(): BatchedFuelBurnCycleValidator {
    return BatchedFuelBurnCycleValidator(
        mutableListOf<BatchedFuelBurnCycleRule>(
            BatchedFuelBurnCycleDurationMinutesRangeRule(120, 3600),
            BatchedFuelBurnCycleCodePrefixRule("REF"),
            BatchedFuelBurnCycleAllowedTitleRule(mutableListOf<String>()),
            BatchedFuelBurnCycleCodeLengthRule(32),
        ),
    )
}
