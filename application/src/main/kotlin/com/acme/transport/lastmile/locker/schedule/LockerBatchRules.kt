package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize

/** A rule a locker batch must pass. Check returns its problems, or none. */
interface LockerBatchRule {
    fun check(item: LockerBatch): MutableList<String>
}

data class LockerBatchRequiredLaneRule(var message: String) : LockerBatchRule {
    override fun check(item: LockerBatch): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerBatchAllowedLaneRule(var allowed: MutableList<String>) : LockerBatchRule {
    override fun check(item: LockerBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LockerBatchRequiredCodeRule(var message: String) : LockerBatchRule {
    override fun check(item: LockerBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerBatchCodePrefixRule(var prefix: String) : LockerBatchRule {
    override fun check(item: LockerBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerBatchUpdatedAtRangeRule(var min: Int, var max: Int) : LockerBatchRule {
    override fun check(item: LockerBatch): MutableList<String> {
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

/** Runs every rule of a locker batch. */
data class LockerBatchValidator(var rules: MutableList<LockerBatchRule>) {
    fun validate(item: LockerBatch): MutableList<String> {
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

    fun requireValid(item: LockerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerBatchValidator(): LockerBatchValidator {
    return LockerBatchValidator(
        mutableListOf<LockerBatchRule>(
            LockerBatchRequiredLaneRule("locker batch needs this field"),
            LockerBatchAllowedLaneRule(mutableListOf<String>("pending")),
            LockerBatchRequiredCodeRule("locker batch needs this field"),
            LockerBatchCodePrefixRule("X"),
            LockerBatchUpdatedAtRangeRule(255, 3600),
        ),
    )
}
