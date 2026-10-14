package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize

/** A rule a accessorial schedule must pass. Check returns its problems, or none. */
interface AccessorialScheduleRule {
    fun check(item: AccessorialSchedule): MutableList<String>
}

data class AccessorialScheduleFloorValueRangeRule(var min: Double, var max: Double) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScheduleRequiredCodeRule(var message: String) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScheduleLaneLengthRule(var maxLength: Int) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScheduleVisibleGuardRule(var limit: Double) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        if (item.visible && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScheduleCodePrefixRule(var prefix: String) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialScheduleAllowedLabelRule(var allowed: MutableList<String>) : AccessorialScheduleRule {
    override fun check(item: AccessorialSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial schedule. */
data class AccessorialScheduleValidator(var rules: MutableList<AccessorialScheduleRule>) {
    fun validate(item: AccessorialSchedule): MutableList<String> {
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

    fun requireValid(item: AccessorialSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialScheduleValidator(): AccessorialScheduleValidator {
    return AccessorialScheduleValidator(
        mutableListOf<AccessorialScheduleRule>(
            AccessorialScheduleFloorValueRangeRule(3.3, 3747.7),
            AccessorialScheduleRequiredCodeRule("accessorial schedule needs a value"),
            AccessorialScheduleLaneLengthRule(3600),
            AccessorialScheduleVisibleGuardRule(5.5),
            AccessorialScheduleCodePrefixRule("REF"),
            AccessorialScheduleAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
