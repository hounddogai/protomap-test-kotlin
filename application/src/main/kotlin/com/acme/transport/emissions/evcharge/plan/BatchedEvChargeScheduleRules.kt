package com.acme.transport.emissions.evcharge.plan

/** A rule a batched ev charge schedule must pass. Check returns its problems, or none. */
interface BatchedEvChargeScheduleRule {
    fun check(item: BatchedEvChargeSchedule): MutableList<String>
}

data class BatchedEvChargeScheduleRequiredOwnerIdRule(var message: String) : BatchedEvChargeScheduleRule {
    override fun check(item: BatchedEvChargeSchedule): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScheduleRequiredCodeRule(var message: String) : BatchedEvChargeScheduleRule {
    override fun check(item: BatchedEvChargeSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScheduleRequiredNameRule(var message: String) : BatchedEvChargeScheduleRule {
    override fun check(item: BatchedEvChargeSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEvChargeScheduleCodePrefixRule(var prefix: String) : BatchedEvChargeScheduleRule {
    override fun check(item: BatchedEvChargeSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched ev charge schedule. */
data class BatchedEvChargeScheduleValidator(var rules: MutableList<BatchedEvChargeScheduleRule>) {
    fun validate(item: BatchedEvChargeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedEvChargeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEvChargeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEvChargeScheduleValidator(): BatchedEvChargeScheduleValidator {
    return BatchedEvChargeScheduleValidator(
        mutableListOf<BatchedEvChargeScheduleRule>(
            BatchedEvChargeScheduleRequiredOwnerIdRule("batched ev charge schedule needs this field"),
            BatchedEvChargeScheduleRequiredCodeRule("batched ev charge schedule needs an entry"),
            BatchedEvChargeScheduleRequiredNameRule("batched ev charge schedule needs this field"),
            BatchedEvChargeScheduleCodePrefixRule("ACM"),
        ),
    )
}
