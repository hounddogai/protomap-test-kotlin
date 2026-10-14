package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.text.normalize

/** A rule a strict dock door schedule must pass. Check returns its problems, or none. */
interface StrictDockDoorScheduleRule {
    fun check(item: StrictDockDoorSchedule): MutableList<String>
}

data class StrictDockDoorScheduleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
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

data class StrictDockDoorScheduleRequiredNameRule(var message: String) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorScheduleNameLengthRule(var maxLength: Int) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorScheduleCodePrefixRule(var prefix: String) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorSchedulePhoneLengthRule(var maxLength: Int) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictDockDoorScheduleRule {
    override fun check(item: StrictDockDoorSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door schedule. */
data class StrictDockDoorScheduleValidator(var rules: MutableList<StrictDockDoorScheduleRule>) {
    fun validate(item: StrictDockDoorSchedule): MutableList<String> {
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

    fun requireValid(item: StrictDockDoorSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorScheduleValidator(): StrictDockDoorScheduleValidator {
    return StrictDockDoorScheduleValidator(
        mutableListOf<StrictDockDoorScheduleRule>(
            StrictDockDoorScheduleFloorValueRangeRule(2.6, 2631.8),
            StrictDockDoorScheduleRequiredNameRule("strict dock door schedule needs an entry"),
            StrictDockDoorScheduleNameLengthRule(86400),
            StrictDockDoorScheduleCodePrefixRule("ACM"),
            StrictDockDoorSchedulePhoneLengthRule(64),
            StrictDockDoorScheduleAllowedCodeRule(mutableListOf<String>("seasonal", "strict", "adaptive")),
        ),
    )
}
