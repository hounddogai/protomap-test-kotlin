package com.acme.transport.linehaul.laneplan.threshold

/** A rule a lane plan threshold must pass. Check returns its problems, or none. */
interface LanePlanThresholdRule {
    fun check(item: LanePlanThreshold): MutableList<String>
}

data class LanePlanThresholdRequiredLocaleRule(var message: String) : LanePlanThresholdRule {
    override fun check(item: LanePlanThreshold): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanThresholdCodeLengthRule(var maxLength: Int) : LanePlanThresholdRule {
    override fun check(item: LanePlanThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanThresholdOdometerRangeRule(var min: Int, var max: Int) : LanePlanThresholdRule {
    override fun check(item: LanePlanThreshold): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanThresholdRequiredRouteCodeRule(var message: String) : LanePlanThresholdRule {
    override fun check(item: LanePlanThreshold): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanThresholdDurationMinutesRangeRule(var min: Int, var max: Int) : LanePlanThresholdRule {
    override fun check(item: LanePlanThreshold): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan threshold. */
data class LanePlanThresholdValidator(var rules: MutableList<LanePlanThresholdRule>) {
    fun validate(item: LanePlanThreshold): MutableList<String> {
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

    fun requireValid(item: LanePlanThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanThresholdValidator(): LanePlanThresholdValidator {
    return LanePlanThresholdValidator(
        mutableListOf<LanePlanThresholdRule>(
            LanePlanThresholdRequiredLocaleRule("lane plan threshold needs an entry"),
            LanePlanThresholdCodeLengthRule(3600),
            LanePlanThresholdOdometerRangeRule(16, 120),
            LanePlanThresholdRequiredRouteCodeRule("lane plan threshold needs this field"),
            LanePlanThresholdDurationMinutesRangeRule(80, 1000),
        ),
    )
}
