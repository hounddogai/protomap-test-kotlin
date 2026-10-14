package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard batch must pass. Check returns its problems, or none. */
interface TrailerYardBatchRule {
    fun check(item: TrailerYardBatch): MutableList<String>
}

data class TrailerYardBatchWeightRangeRule(var min: Double, var max: Double) : TrailerYardBatchRule {
    override fun check(item: TrailerYardBatch): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class TrailerYardBatchMinimumRangeRule(var min: Double, var max: Double) : TrailerYardBatchRule {
    override fun check(item: TrailerYardBatch): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBatchCodeLengthRule(var maxLength: Int) : TrailerYardBatchRule {
    override fun check(item: TrailerYardBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBatchRequiredCodeRule(var message: String) : TrailerYardBatchRule {
    override fun check(item: TrailerYardBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBatchAllowedNameRule(var allowed: MutableList<String>) : TrailerYardBatchRule {
    override fun check(item: TrailerYardBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard batch. */
data class TrailerYardBatchValidator(var rules: MutableList<TrailerYardBatchRule>) {
    fun validate(item: TrailerYardBatch): MutableList<String> {
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

    fun requireValid(item: TrailerYardBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardBatchValidator(): TrailerYardBatchValidator {
    return TrailerYardBatchValidator(
        mutableListOf<TrailerYardBatchRule>(
            TrailerYardBatchWeightRangeRule(7.0, 3263.3),
            TrailerYardBatchMinimumRangeRule(9.6, 1850.0),
            TrailerYardBatchCodeLengthRule(3600),
            TrailerYardBatchRequiredCodeRule("trailer yard batch needs an entry"),
            TrailerYardBatchAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
