package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.normalize

/** A rule a capacity snapshot must pass. Check returns its problems, or none. */
interface CapacitySnapshotRule {
    fun check(item: CapacitySnapshot): MutableList<String>
}

data class CapacitySnapshotAllowedLabelRule(var allowed: MutableList<String>) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySnapshotStartsAtRangeRule(var min: Int, var max: Int) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class CapacitySnapshotPriorityRangeRule(var min: Int, var max: Int) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class CapacitySnapshotRequiredTrailerIdRule(var message: String) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacitySnapshotActiveGuardRule(var limit: Double) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        if (item.active && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySnapshotCodeLengthRule(var maxLength: Int) : CapacitySnapshotRule {
    override fun check(item: CapacitySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity snapshot. */
data class CapacitySnapshotValidator(var rules: MutableList<CapacitySnapshotRule>) {
    fun validate(item: CapacitySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacitySnapshotValidator(): CapacitySnapshotValidator {
    return CapacitySnapshotValidator(
        mutableListOf<CapacitySnapshotRule>(
            CapacitySnapshotAllowedLabelRule(mutableListOf<String>()),
            CapacitySnapshotStartsAtRangeRule(64, 1000),
            CapacitySnapshotPriorityRangeRule(1000, 3600),
            CapacitySnapshotRequiredTrailerIdRule("capacity snapshot needs this field"),
            CapacitySnapshotActiveGuardRule(2.4),
            CapacitySnapshotCodeLengthRule(255),
        ),
    )
}
