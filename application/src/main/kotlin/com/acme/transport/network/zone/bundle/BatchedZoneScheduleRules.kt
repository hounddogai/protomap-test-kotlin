package com.acme.transport.network.zone.bundle

/** A rule a batched zone schedule must pass. Check returns its problems, or none. */
interface BatchedZoneScheduleRule {
    fun check(item: BatchedZoneSchedule): MutableList<String>
}

data class BatchedZoneScheduleRequiredCarrierRule(var message: String) : BatchedZoneScheduleRule {
    override fun check(item: BatchedZoneSchedule): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneScheduleTitleLengthRule(var maxLength: Int) : BatchedZoneScheduleRule {
    override fun check(item: BatchedZoneSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneScheduleUpdatedAtRangeRule(var min: Int, var max: Int) : BatchedZoneScheduleRule {
    override fun check(item: BatchedZoneSchedule): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class BatchedZoneScheduleRequiredCodeRule(var message: String) : BatchedZoneScheduleRule {
    override fun check(item: BatchedZoneSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneScheduleActiveGuardRule(var limit: Int) : BatchedZoneScheduleRule {
    override fun check(item: BatchedZoneSchedule): MutableList<String> {
        if (item.active && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched zone schedule. */
data class BatchedZoneScheduleValidator(var rules: MutableList<BatchedZoneScheduleRule>) {
    fun validate(item: BatchedZoneSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedZoneSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedZoneSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedZoneScheduleValidator(): BatchedZoneScheduleValidator {
    return BatchedZoneScheduleValidator(
        mutableListOf<BatchedZoneScheduleRule>(
            BatchedZoneScheduleRequiredCarrierRule("batched zone schedule needs a value"),
            BatchedZoneScheduleTitleLengthRule(1000),
            BatchedZoneScheduleUpdatedAtRangeRule(86400, 86400),
            BatchedZoneScheduleRequiredCodeRule("batched zone schedule needs a value"),
            BatchedZoneScheduleActiveGuardRule(3600),
        ),
    )
}
