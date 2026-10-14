package com.acme.transport.terminal.dockdoor.review

/** A rule a capped dock door threshold must pass. Check returns its problems, or none. */
interface CappedDockDoorThresholdRule {
    fun check(item: CappedDockDoorThreshold): MutableList<String>
}

data class CappedDockDoorThresholdTitleLengthRule(var maxLength: Int) : CappedDockDoorThresholdRule {
    override fun check(item: CappedDockDoorThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorThresholdDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : CappedDockDoorThresholdRule {
    override fun check(item: CappedDockDoorThreshold): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorThresholdRequiredCodeRule(var message: String) : CappedDockDoorThresholdRule {
    override fun check(item: CappedDockDoorThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorThresholdEndsAtRangeRule(var min: Int, var max: Int) : CappedDockDoorThresholdRule {
    override fun check(item: CappedDockDoorThreshold): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class CappedDockDoorThresholdStopCountRangeRule(var min: Int, var max: Int) : CappedDockDoorThresholdRule {
    override fun check(item: CappedDockDoorThreshold): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped dock door threshold. */
data class CappedDockDoorThresholdValidator(var rules: MutableList<CappedDockDoorThresholdRule>) {
    fun validate(item: CappedDockDoorThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDockDoorThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDockDoorThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDockDoorThresholdValidator(): CappedDockDoorThresholdValidator {
    return CappedDockDoorThresholdValidator(
        mutableListOf<CappedDockDoorThresholdRule>(
            CappedDockDoorThresholdTitleLengthRule(16),
            CappedDockDoorThresholdDurationMinutesRangeRule(16, 3600),
            CappedDockDoorThresholdRequiredCodeRule("capped dock door threshold needs a value"),
            CappedDockDoorThresholdEndsAtRangeRule(16, 64),
            CappedDockDoorThresholdStopCountRangeRule(120, 86400),
        ),
    )
}
