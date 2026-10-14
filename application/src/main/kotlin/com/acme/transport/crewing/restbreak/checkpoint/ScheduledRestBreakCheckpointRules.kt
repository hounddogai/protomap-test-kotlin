package com.acme.transport.crewing.restbreak.checkpoint

/** A rule a scheduled rest break checkpoint must pass. Check returns its problems, or none. */
interface ScheduledRestBreakCheckpointRule {
    fun check(item: ScheduledRestBreakCheckpoint): MutableList<String>
}

data class ScheduledRestBreakCheckpointMarginRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledRestBreakCheckpointRule {
    override fun check(item: ScheduledRestBreakCheckpoint): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ScheduledRestBreakCheckpointRequiredRouteCodeRule(
    var message: String,
) : ScheduledRestBreakCheckpointRule {
    override fun check(item: ScheduledRestBreakCheckpoint): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakCheckpointRequiredNameRule(
    var message: String,
) : ScheduledRestBreakCheckpointRule {
    override fun check(item: ScheduledRestBreakCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRestBreakCheckpointRouteCodeLengthRule(
    var maxLength: Int,
) : ScheduledRestBreakCheckpointRule {
    override fun check(item: ScheduledRestBreakCheckpoint): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rest break checkpoint. */
data class ScheduledRestBreakCheckpointValidator(var rules: MutableList<ScheduledRestBreakCheckpointRule>) {
    fun validate(item: ScheduledRestBreakCheckpoint): MutableList<String> {
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

    fun requireValid(item: ScheduledRestBreakCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRestBreakCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRestBreakCheckpointValidator(): ScheduledRestBreakCheckpointValidator {
    return ScheduledRestBreakCheckpointValidator(
        mutableListOf<ScheduledRestBreakCheckpointRule>(
            ScheduledRestBreakCheckpointMarginRangeRule(5.8, 2629.5),
            ScheduledRestBreakCheckpointRequiredRouteCodeRule("scheduled rest break checkpoint needs a value"),
            ScheduledRestBreakCheckpointRequiredNameRule("scheduled rest break checkpoint needs this field"),
            ScheduledRestBreakCheckpointRouteCodeLengthRule(1000),
        ),
    )
}
