package com.acme.transport.crewing.restbreak.breakmilestone

/** A rule a strict rest break checkpoint must pass. Check returns its problems, or none. */
interface StrictRestBreakCheckpointRule {
    fun check(item: StrictRestBreakCheckpoint): MutableList<String>
}

data class StrictRestBreakCheckpointCodePrefixRule(var prefix: String) : StrictRestBreakCheckpointRule {
    override fun check(item: StrictRestBreakCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakCheckpointWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : StrictRestBreakCheckpointRule {
    override fun check(item: StrictRestBreakCheckpoint): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakCheckpointMinimumRangeRule(
    var min: Double,
    var max: Double,
) : StrictRestBreakCheckpointRule {
    override fun check(item: StrictRestBreakCheckpoint): MutableList<String> {
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

data class StrictRestBreakCheckpointCodeLengthRule(var maxLength: Int) : StrictRestBreakCheckpointRule {
    override fun check(item: StrictRestBreakCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakCheckpointVersionRangeRule(
    var min: Int,
    var max: Int,
) : StrictRestBreakCheckpointRule {
    override fun check(item: StrictRestBreakCheckpoint): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rest break checkpoint. */
data class StrictRestBreakCheckpointValidator(var rules: MutableList<StrictRestBreakCheckpointRule>) {
    fun validate(item: StrictRestBreakCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRestBreakCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRestBreakCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRestBreakCheckpointValidator(): StrictRestBreakCheckpointValidator {
    return StrictRestBreakCheckpointValidator(
        mutableListOf<StrictRestBreakCheckpointRule>(
            StrictRestBreakCheckpointCodePrefixRule("X"),
            StrictRestBreakCheckpointWindowDaysRangeRule(32, 64),
            StrictRestBreakCheckpointMinimumRangeRule(0.5, 2105.6),
            StrictRestBreakCheckpointCodeLengthRule(1000),
            StrictRestBreakCheckpointVersionRangeRule(64, 255),
        ),
    )
}
