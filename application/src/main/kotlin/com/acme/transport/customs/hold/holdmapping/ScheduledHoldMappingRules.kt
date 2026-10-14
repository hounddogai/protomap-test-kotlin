package com.acme.transport.customs.hold.holdmapping

/** A rule a scheduled hold mapping must pass. Check returns its problems, or none. */
interface ScheduledHoldMappingRule {
    fun check(item: ScheduledHoldMapping): MutableList<String>
}

data class ScheduledHoldMappingCodeLengthRule(var maxLength: Int) : ScheduledHoldMappingRule {
    override fun check(item: ScheduledHoldMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMappingLabelLengthRule(var maxLength: Int) : ScheduledHoldMappingRule {
    override fun check(item: ScheduledHoldMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMappingThresholdRangeRule(var min: Double, var max: Double) : ScheduledHoldMappingRule {
    override fun check(item: ScheduledHoldMapping): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldMappingApprovedGuardRule(var limit: Double) : ScheduledHoldMappingRule {
    override fun check(item: ScheduledHoldMapping): MutableList<String> {
        if (item.approved && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold mapping. */
data class ScheduledHoldMappingValidator(var rules: MutableList<ScheduledHoldMappingRule>) {
    fun validate(item: ScheduledHoldMapping): MutableList<String> {
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

    fun requireValid(item: ScheduledHoldMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldMappingValidator(): ScheduledHoldMappingValidator {
    return ScheduledHoldMappingValidator(
        mutableListOf<ScheduledHoldMappingRule>(
            ScheduledHoldMappingCodeLengthRule(64),
            ScheduledHoldMappingLabelLengthRule(64),
            ScheduledHoldMappingThresholdRangeRule(9.7, 3163.3),
            ScheduledHoldMappingApprovedGuardRule(1.1),
        ),
    )
}
