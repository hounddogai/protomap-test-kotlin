package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize

/** A rule a capacity schedule must pass. Check returns its problems, or none. */
interface CapacityScheduleRule {
    fun check(item: CapacitySchedule): MutableList<String>
}

data class CapacityScheduleRequiredCodeRule(var message: String) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityScheduleAllowedCodeRule(var allowed: MutableList<String>) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CapacityScheduleNameLengthRule(var maxLength: Int) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityScheduleUnitLengthRule(var maxLength: Int) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityScheduleRequiredNameRule(var message: String) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityScheduleEtaMinutesRangeRule(var min: Int, var max: Int) : CapacityScheduleRule {
    override fun check(item: CapacitySchedule): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity schedule. */
data class CapacityScheduleValidator(var rules: MutableList<CapacityScheduleRule>) {
    fun validate(item: CapacitySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacitySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacitySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityScheduleValidator(): CapacityScheduleValidator {
    return CapacityScheduleValidator(
        mutableListOf<CapacityScheduleRule>(
            CapacityScheduleRequiredCodeRule("capacity schedule needs an entry"),
            CapacityScheduleAllowedCodeRule(mutableListOf<String>("capped", "batched")),
            CapacityScheduleNameLengthRule(255),
            CapacityScheduleUnitLengthRule(120),
            CapacityScheduleRequiredNameRule("capacity schedule needs a value"),
            CapacityScheduleEtaMinutesRangeRule(80, 255),
        ),
    )
}
