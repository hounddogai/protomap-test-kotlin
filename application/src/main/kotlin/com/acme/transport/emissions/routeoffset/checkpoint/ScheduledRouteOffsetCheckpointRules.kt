package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a scheduled route offset checkpoint must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetCheckpointRule {
    fun check(item: ScheduledRouteOffsetCheckpoint): MutableList<String>
}

data class ScheduledRouteOffsetCheckpointExternalRefLengthRule(
    var maxLength: Int,
) : ScheduledRouteOffsetCheckpointRule {
    override fun check(item: ScheduledRouteOffsetCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetCheckpointNameLengthRule(
    var maxLength: Int,
) : ScheduledRouteOffsetCheckpointRule {
    override fun check(item: ScheduledRouteOffsetCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetCheckpointAllowedDepotRule(
    var allowed: MutableList<String>,
) : ScheduledRouteOffsetCheckpointRule {
    override fun check(item: ScheduledRouteOffsetCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetCheckpointRequiredNameRule(
    var message: String,
) : ScheduledRouteOffsetCheckpointRule {
    override fun check(item: ScheduledRouteOffsetCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled route offset checkpoint. */
data class ScheduledRouteOffsetCheckpointValidator(var rules: MutableList<ScheduledRouteOffsetCheckpointRule>) {
    fun validate(item: ScheduledRouteOffsetCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRouteOffsetCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetCheckpointValidator(): ScheduledRouteOffsetCheckpointValidator {
    return ScheduledRouteOffsetCheckpointValidator(
        mutableListOf<ScheduledRouteOffsetCheckpointRule>(
            ScheduledRouteOffsetCheckpointExternalRefLengthRule(255),
            ScheduledRouteOffsetCheckpointNameLengthRule(255),
            ScheduledRouteOffsetCheckpointAllowedDepotRule(mutableListOf<String>()),
            ScheduledRouteOffsetCheckpointRequiredNameRule("scheduled route offset checkpoint needs an entry"),
        ),
    )
}
