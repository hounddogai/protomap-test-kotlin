package com.acme.transport.safety.nearmiss.notice

/** A rule a scheduled near miss override must pass. Check returns its problems, or none. */
interface ScheduledNearMissOverrideRule {
    fun check(item: ScheduledNearMissOverride): MutableList<String>
}

data class ScheduledNearMissOverrideCodeLengthRule(var maxLength: Int) : ScheduledNearMissOverrideRule {
    override fun check(item: ScheduledNearMissOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissOverrideWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledNearMissOverrideRule {
    override fun check(item: ScheduledNearMissOverride): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ScheduledNearMissOverrideRequiredUnitRule(var message: String) : ScheduledNearMissOverrideRule {
    override fun check(item: ScheduledNearMissOverride): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss override. */
data class ScheduledNearMissOverrideValidator(var rules: MutableList<ScheduledNearMissOverrideRule>) {
    fun validate(item: ScheduledNearMissOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledNearMissOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissOverrideValidator(): ScheduledNearMissOverrideValidator {
    return ScheduledNearMissOverrideValidator(
        mutableListOf<ScheduledNearMissOverrideRule>(
            ScheduledNearMissOverrideCodeLengthRule(80),
            ScheduledNearMissOverrideWeightRangeRule(3.4, 2458.4),
            ScheduledNearMissOverrideRequiredUnitRule("scheduled near miss override needs this field"),
        ),
    )
}
