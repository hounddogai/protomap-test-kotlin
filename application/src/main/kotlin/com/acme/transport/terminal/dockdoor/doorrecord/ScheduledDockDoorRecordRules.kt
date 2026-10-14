package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dock door record must pass. Check returns its problems, or none. */
interface ScheduledDockDoorRecordRule {
    fun check(item: ScheduledDockDoorRecord): MutableList<String>
}

data class ScheduledDockDoorRecordCodePrefixRule(var prefix: String) : ScheduledDockDoorRecordRule {
    override fun check(item: ScheduledDockDoorRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorRecordFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDockDoorRecordRule {
    override fun check(item: ScheduledDockDoorRecord): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ScheduledDockDoorRecordNameLengthRule(var maxLength: Int) : ScheduledDockDoorRecordRule {
    override fun check(item: ScheduledDockDoorRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorRecordRequiredNameRule(var message: String) : ScheduledDockDoorRecordRule {
    override fun check(item: ScheduledDockDoorRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorRecordAllowedNotesRule(
    var allowed: MutableList<String>,
) : ScheduledDockDoorRecordRule {
    override fun check(item: ScheduledDockDoorRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door record. */
data class ScheduledDockDoorRecordValidator(var rules: MutableList<ScheduledDockDoorRecordRule>) {
    fun validate(item: ScheduledDockDoorRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDockDoorRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorRecordValidator(): ScheduledDockDoorRecordValidator {
    return ScheduledDockDoorRecordValidator(
        mutableListOf<ScheduledDockDoorRecordRule>(
            ScheduledDockDoorRecordCodePrefixRule("OPS"),
            ScheduledDockDoorRecordFloorValueRangeRule(3.8, 1349.8),
            ScheduledDockDoorRecordNameLengthRule(3600),
            ScheduledDockDoorRecordRequiredNameRule("scheduled dock door record needs an entry"),
            ScheduledDockDoorRecordAllowedNotesRule(mutableListOf<String>()),
        ),
    )
}
