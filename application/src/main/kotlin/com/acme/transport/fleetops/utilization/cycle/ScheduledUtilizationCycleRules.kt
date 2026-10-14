package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.text.normalize

/** A rule a scheduled utilization cycle must pass. Check returns its problems, or none. */
interface ScheduledUtilizationCycleRule {
    fun check(item: ScheduledUtilizationCycle): MutableList<String>
}

data class ScheduledUtilizationCycleVisibleGuardRule(var limit: Int) : ScheduledUtilizationCycleRule {
    override fun check(item: ScheduledUtilizationCycle): MutableList<String> {
        if (item.visible && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationCycleRequiredNameRule(var message: String) : ScheduledUtilizationCycleRule {
    override fun check(item: ScheduledUtilizationCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationCycleAllowedChecksumRule(
    var allowed: MutableList<String>,
) : ScheduledUtilizationCycleRule {
    override fun check(item: ScheduledUtilizationCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationCycleRequiredCodeRule(var message: String) : ScheduledUtilizationCycleRule {
    override fun check(item: ScheduledUtilizationCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledUtilizationCycleRequiredChecksumRule(var message: String) : ScheduledUtilizationCycleRule {
    override fun check(item: ScheduledUtilizationCycle): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled utilization cycle. */
data class ScheduledUtilizationCycleValidator(var rules: MutableList<ScheduledUtilizationCycleRule>) {
    fun validate(item: ScheduledUtilizationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledUtilizationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledUtilizationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledUtilizationCycleValidator(): ScheduledUtilizationCycleValidator {
    return ScheduledUtilizationCycleValidator(
        mutableListOf<ScheduledUtilizationCycleRule>(
            ScheduledUtilizationCycleVisibleGuardRule(1000),
            ScheduledUtilizationCycleRequiredNameRule("scheduled utilization cycle needs an entry"),
            ScheduledUtilizationCycleAllowedChecksumRule(mutableListOf<String>("global", "default", "pending")),
            ScheduledUtilizationCycleRequiredCodeRule("scheduled utilization cycle needs a value"),
            ScheduledUtilizationCycleRequiredChecksumRule("scheduled utilization cycle needs an entry"),
        ),
    )
}
