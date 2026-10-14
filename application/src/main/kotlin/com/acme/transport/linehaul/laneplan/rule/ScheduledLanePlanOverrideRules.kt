package com.acme.transport.linehaul.laneplan.rule

/** A rule a scheduled lane plan override must pass. Check returns its problems, or none. */
interface ScheduledLanePlanOverrideRule {
    fun check(item: ScheduledLanePlanOverride): MutableList<String>
}

data class ScheduledLanePlanOverrideWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLanePlanOverrideRule {
    override fun check(item: ScheduledLanePlanOverride): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanOverrideRequiredNameRule(var message: String) : ScheduledLanePlanOverrideRule {
    override fun check(item: ScheduledLanePlanOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanOverrideCodeLengthRule(var maxLength: Int) : ScheduledLanePlanOverrideRule {
    override fun check(item: ScheduledLanePlanOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lane plan override. */
data class ScheduledLanePlanOverrideValidator(var rules: MutableList<ScheduledLanePlanOverrideRule>) {
    fun validate(item: ScheduledLanePlanOverride): MutableList<String> {
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

    fun requireValid(item: ScheduledLanePlanOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanOverrideValidator(): ScheduledLanePlanOverrideValidator {
    return ScheduledLanePlanOverrideValidator(
        mutableListOf<ScheduledLanePlanOverrideRule>(
            ScheduledLanePlanOverrideWeightRangeRule(4.1, 2264.3),
            ScheduledLanePlanOverrideRequiredNameRule("scheduled lane plan override needs an entry"),
            ScheduledLanePlanOverrideCodeLengthRule(3600),
        ),
    )
}
