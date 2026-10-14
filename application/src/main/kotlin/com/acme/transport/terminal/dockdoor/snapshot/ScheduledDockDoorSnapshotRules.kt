package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dock door snapshot must pass. Check returns its problems, or none. */
interface ScheduledDockDoorSnapshotRule {
    fun check(item: ScheduledDockDoorSnapshot): MutableList<String>
}

data class ScheduledDockDoorSnapshotExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledDockDoorSnapshotRule {
    override fun check(item: ScheduledDockDoorSnapshot): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorSnapshotRequiredDescriptionRule(
    var message: String,
) : ScheduledDockDoorSnapshotRule {
    override fun check(item: ScheduledDockDoorSnapshot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorSnapshotAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledDockDoorSnapshotRule {
    override fun check(item: ScheduledDockDoorSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorSnapshotDescriptionLengthRule(var maxLength: Int) : ScheduledDockDoorSnapshotRule {
    override fun check(item: ScheduledDockDoorSnapshot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door snapshot. */
data class ScheduledDockDoorSnapshotValidator(var rules: MutableList<ScheduledDockDoorSnapshotRule>) {
    fun validate(item: ScheduledDockDoorSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDockDoorSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorSnapshotValidator(): ScheduledDockDoorSnapshotValidator {
    return ScheduledDockDoorSnapshotValidator(
        mutableListOf<ScheduledDockDoorSnapshotRule>(
            ScheduledDockDoorSnapshotExpiresAtRangeRule(80, 3600),
            ScheduledDockDoorSnapshotRequiredDescriptionRule("scheduled dock door snapshot needs an entry"),
            ScheduledDockDoorSnapshotAllowedCodeRule(mutableListOf<String>("primary", "rolling")),
            ScheduledDockDoorSnapshotDescriptionLengthRule(32),
        ),
    )
}
