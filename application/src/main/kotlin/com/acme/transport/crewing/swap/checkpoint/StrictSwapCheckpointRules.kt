package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a strict swap checkpoint must pass. Check returns its problems, or none. */
interface StrictSwapCheckpointRule {
    fun check(item: StrictSwapCheckpoint): MutableList<String>
}

data class StrictSwapCheckpointCodeLengthRule(var maxLength: Int) : StrictSwapCheckpointRule {
    override fun check(item: StrictSwapCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapCheckpointRequiredTitleRule(var message: String) : StrictSwapCheckpointRule {
    override fun check(item: StrictSwapCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapCheckpointGraceDaysRangeRule(var min: Int, var max: Int) : StrictSwapCheckpointRule {
    override fun check(item: StrictSwapCheckpoint): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapCheckpointWindowDaysRangeRule(var min: Int, var max: Int) : StrictSwapCheckpointRule {
    override fun check(item: StrictSwapCheckpoint): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class StrictSwapCheckpointAllowedCodeRule(var allowed: MutableList<String>) : StrictSwapCheckpointRule {
    override fun check(item: StrictSwapCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict swap checkpoint. */
data class StrictSwapCheckpointValidator(var rules: MutableList<StrictSwapCheckpointRule>) {
    fun validate(item: StrictSwapCheckpoint): MutableList<String> {
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

    fun requireValid(item: StrictSwapCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSwapCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSwapCheckpointValidator(): StrictSwapCheckpointValidator {
    return StrictSwapCheckpointValidator(
        mutableListOf<StrictSwapCheckpointRule>(
            StrictSwapCheckpointCodeLengthRule(80),
            StrictSwapCheckpointRequiredTitleRule("strict swap checkpoint needs this field"),
            StrictSwapCheckpointGraceDaysRangeRule(64, 1000),
            StrictSwapCheckpointWindowDaysRangeRule(64, 255),
            StrictSwapCheckpointAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
