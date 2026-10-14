package com.acme.transport.lastmile.redelivery.checkpoint

/** A rule a redelivery checkpoint must pass. Check returns its problems, or none. */
interface RedeliveryCheckpointRule {
    fun check(item: RedeliveryCheckpoint): MutableList<String>
}

data class RedeliveryCheckpointMinimumRangeRule(var min: Double, var max: Double) : RedeliveryCheckpointRule {
    override fun check(item: RedeliveryCheckpoint): MutableList<String> {
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

data class RedeliveryCheckpointRatioRangeRule(var min: Double, var max: Double) : RedeliveryCheckpointRule {
    override fun check(item: RedeliveryCheckpoint): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RedeliveryCheckpointRequiredLaneRule(var message: String) : RedeliveryCheckpointRule {
    override fun check(item: RedeliveryCheckpoint): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryCheckpointGraceDaysRangeRule(var min: Int, var max: Int) : RedeliveryCheckpointRule {
    override fun check(item: RedeliveryCheckpoint): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RedeliveryCheckpointExternalRefLengthRule(var maxLength: Int) : RedeliveryCheckpointRule {
    override fun check(item: RedeliveryCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery checkpoint. */
data class RedeliveryCheckpointValidator(var rules: MutableList<RedeliveryCheckpointRule>) {
    fun validate(item: RedeliveryCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryCheckpointValidator(): RedeliveryCheckpointValidator {
    return RedeliveryCheckpointValidator(
        mutableListOf<RedeliveryCheckpointRule>(
            RedeliveryCheckpointMinimumRangeRule(5.7, 1041.0),
            RedeliveryCheckpointRatioRangeRule(2.1, 4357.6),
            RedeliveryCheckpointRequiredLaneRule("redelivery checkpoint needs a value"),
            RedeliveryCheckpointGraceDaysRangeRule(16, 80),
            RedeliveryCheckpointExternalRefLengthRule(64),
        ),
    )
}
