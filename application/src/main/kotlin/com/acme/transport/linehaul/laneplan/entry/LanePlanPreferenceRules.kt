package com.acme.transport.linehaul.laneplan.entry

/** A rule a lane plan preference must pass. Check returns its problems, or none. */
interface LanePlanPreferenceRule {
    fun check(item: LanePlanPreference): MutableList<String>
}

data class LanePlanPreferenceCapacityRangeRule(var min: Int, var max: Int) : LanePlanPreferenceRule {
    override fun check(item: LanePlanPreference): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LanePlanPreferenceExternalRefLengthRule(var maxLength: Int) : LanePlanPreferenceRule {
    override fun check(item: LanePlanPreference): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPreferenceCodeLengthRule(var maxLength: Int) : LanePlanPreferenceRule {
    override fun check(item: LanePlanPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPreferenceRatioRangeRule(var min: Double, var max: Double) : LanePlanPreferenceRule {
    override fun check(item: LanePlanPreference): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan preference. */
data class LanePlanPreferenceValidator(var rules: MutableList<LanePlanPreferenceRule>) {
    fun validate(item: LanePlanPreference): MutableList<String> {
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

    fun requireValid(item: LanePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanPreferenceValidator(): LanePlanPreferenceValidator {
    return LanePlanPreferenceValidator(
        mutableListOf<LanePlanPreferenceRule>(
            LanePlanPreferenceCapacityRangeRule(32, 1000),
            LanePlanPreferenceExternalRefLengthRule(1000),
            LanePlanPreferenceCodeLengthRule(64),
            LanePlanPreferenceRatioRangeRule(8.0, 1473.2),
        ),
    )
}
