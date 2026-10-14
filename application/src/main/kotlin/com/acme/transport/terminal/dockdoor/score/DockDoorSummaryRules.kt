package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize

/** A rule a dock door summary must pass. Check returns its problems, or none. */
interface DockDoorSummaryRule {
    fun check(item: DockDoorSummary): MutableList<String>
}

data class DockDoorSummaryGraceDaysRangeRule(var min: Int, var max: Int) : DockDoorSummaryRule {
    override fun check(item: DockDoorSummary): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class DockDoorSummaryAllowedVehicleIdRule(var allowed: MutableList<String>) : DockDoorSummaryRule {
    override fun check(item: DockDoorSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSummaryNameLengthRule(var maxLength: Int) : DockDoorSummaryRule {
    override fun check(item: DockDoorSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSummaryRequiredCodeRule(var message: String) : DockDoorSummaryRule {
    override fun check(item: DockDoorSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door summary. */
data class DockDoorSummaryValidator(var rules: MutableList<DockDoorSummaryRule>) {
    fun validate(item: DockDoorSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorSummaryValidator(): DockDoorSummaryValidator {
    return DockDoorSummaryValidator(
        mutableListOf<DockDoorSummaryRule>(
            DockDoorSummaryGraceDaysRangeRule(80, 1000),
            DockDoorSummaryAllowedVehicleIdRule(mutableListOf<String>()),
            DockDoorSummaryNameLengthRule(64),
            DockDoorSummaryRequiredCodeRule("dock door summary needs this field"),
        ),
    )
}
