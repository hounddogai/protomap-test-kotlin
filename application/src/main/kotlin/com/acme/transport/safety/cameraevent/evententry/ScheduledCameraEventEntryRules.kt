package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.text.normalize

/** A rule a scheduled camera event entry must pass. Check returns its problems, or none. */
interface ScheduledCameraEventEntryRule {
    fun check(item: ScheduledCameraEventEntry): MutableList<String>
}

data class ScheduledCameraEventEntryCapacityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCameraEventEntryRule {
    override fun check(item: ScheduledCameraEventEntry): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEntryTitleLengthRule(var maxLength: Int) : ScheduledCameraEventEntryRule {
    override fun check(item: ScheduledCameraEventEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEntryRequiredCodeRule(var message: String) : ScheduledCameraEventEntryRule {
    override fun check(item: ScheduledCameraEventEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEntryRequiredDepotRule(var message: String) : ScheduledCameraEventEntryRule {
    override fun check(item: ScheduledCameraEventEntry): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCameraEventEntryAllowedDepotRule(
    var allowed: MutableList<String>,
) : ScheduledCameraEventEntryRule {
    override fun check(item: ScheduledCameraEventEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled camera event entry. */
data class ScheduledCameraEventEntryValidator(var rules: MutableList<ScheduledCameraEventEntryRule>) {
    fun validate(item: ScheduledCameraEventEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCameraEventEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCameraEventEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCameraEventEntryValidator(): ScheduledCameraEventEntryValidator {
    return ScheduledCameraEventEntryValidator(
        mutableListOf<ScheduledCameraEventEntryRule>(
            ScheduledCameraEventEntryCapacityRangeRule(32, 120),
            ScheduledCameraEventEntryTitleLengthRule(64),
            ScheduledCameraEventEntryRequiredCodeRule("scheduled camera event entry needs an entry"),
            ScheduledCameraEventEntryRequiredDepotRule("scheduled camera event entry needs a value"),
            ScheduledCameraEventEntryAllowedDepotRule(mutableListOf<String>("fallback", "weighted")),
        ),
    )
}
