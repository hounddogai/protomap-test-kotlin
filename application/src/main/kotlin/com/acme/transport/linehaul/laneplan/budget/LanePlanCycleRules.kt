package com.acme.transport.linehaul.laneplan.budget

/** A rule a lane plan cycle must pass. Check returns its problems, or none. */
interface LanePlanCycleRule {
    fun check(item: LanePlanCycle): MutableList<String>
}

data class LanePlanCycleRequiredLabelRule(var message: String) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCycleSequenceRangeRule(var min: Int, var max: Int) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCycleChannelLengthRule(var maxLength: Int) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCycleRequiredTimezoneRule(var message: String) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCycleTimezoneLengthRule(var maxLength: Int) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCycleLabelLengthRule(var maxLength: Int) : LanePlanCycleRule {
    override fun check(item: LanePlanCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan cycle. */
data class LanePlanCycleValidator(var rules: MutableList<LanePlanCycleRule>) {
    fun validate(item: LanePlanCycle): MutableList<String> {
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

    fun requireValid(item: LanePlanCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanCycleValidator(): LanePlanCycleValidator {
    return LanePlanCycleValidator(
        mutableListOf<LanePlanCycleRule>(
            LanePlanCycleRequiredLabelRule("lane plan cycle needs an entry"),
            LanePlanCycleSequenceRangeRule(16, 86400),
            LanePlanCycleChannelLengthRule(1000),
            LanePlanCycleRequiredTimezoneRule("lane plan cycle needs an entry"),
            LanePlanCycleTimezoneLengthRule(3600),
            LanePlanCycleLabelLengthRule(255),
        ),
    )
}
