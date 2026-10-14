package com.acme.transport.linehaul.relay.digest

/** A rule a relay schedule must pass. Check returns its problems, or none. */
interface RelayScheduleRule {
    fun check(item: RelaySchedule): MutableList<String>
}

data class RelayScheduleCapacityRangeRule(var min: Int, var max: Int) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayScheduleCodePrefixRule(var prefix: String) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayScheduleRequiredLabelRule(var message: String) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayScheduleRequiredTimezoneRule(var message: String) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayScheduleLabelLengthRule(var maxLength: Int) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayScheduleTimezoneLengthRule(var maxLength: Int) : RelayScheduleRule {
    override fun check(item: RelaySchedule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay schedule. */
data class RelayScheduleValidator(var rules: MutableList<RelayScheduleRule>) {
    fun validate(item: RelaySchedule): MutableList<String> {
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

    fun requireValid(item: RelaySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelaySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayScheduleValidator(): RelayScheduleValidator {
    return RelayScheduleValidator(
        mutableListOf<RelayScheduleRule>(
            RelayScheduleCapacityRangeRule(16, 1000),
            RelayScheduleCodePrefixRule("OPS"),
            RelayScheduleRequiredLabelRule("relay schedule needs this field"),
            RelayScheduleRequiredTimezoneRule("relay schedule needs an entry"),
            RelayScheduleLabelLengthRule(64),
            RelayScheduleTimezoneLengthRule(80),
        ),
    )
}
