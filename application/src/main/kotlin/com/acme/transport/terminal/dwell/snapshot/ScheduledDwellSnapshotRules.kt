package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dwell snapshot must pass. Check returns its problems, or none. */
interface ScheduledDwellSnapshotRule {
    fun check(item: ScheduledDwellSnapshot): MutableList<String>
}

data class ScheduledDwellSnapshotAllowedChecksumRule(
    var allowed: MutableList<String>,
) : ScheduledDwellSnapshotRule {
    override fun check(item: ScheduledDwellSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellSnapshotRequiredCodeRule(var message: String) : ScheduledDwellSnapshotRule {
    override fun check(item: ScheduledDwellSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellSnapshotRequiredLabelRule(var message: String) : ScheduledDwellSnapshotRule {
    override fun check(item: ScheduledDwellSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dwell snapshot. */
data class ScheduledDwellSnapshotValidator(var rules: MutableList<ScheduledDwellSnapshotRule>) {
    fun validate(item: ScheduledDwellSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDwellSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDwellSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDwellSnapshotValidator(): ScheduledDwellSnapshotValidator {
    return ScheduledDwellSnapshotValidator(
        mutableListOf<ScheduledDwellSnapshotRule>(
            ScheduledDwellSnapshotAllowedChecksumRule(mutableListOf<String>("lenient")),
            ScheduledDwellSnapshotRequiredCodeRule("scheduled dwell snapshot needs a value"),
            ScheduledDwellSnapshotRequiredLabelRule("scheduled dwell snapshot needs an entry"),
        ),
    )
}
