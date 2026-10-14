package com.acme.transport.safety.scorecard.scorecardnotice

/** A rule a capped scorecard schedule must pass. Check returns its problems, or none. */
interface CappedScorecardScheduleRule {
    fun check(item: CappedScorecardSchedule): MutableList<String>
}

data class CappedScorecardScheduleCodePrefixRule(var prefix: String) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardScheduleRequiredRouteCodeRule(var message: String) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardScheduleRequiredChannelRule(var message: String) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardScheduleCodeLengthRule(var maxLength: Int) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardScheduleQuantityRangeRule(var min: Int, var max: Int) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class CappedScorecardScheduleMaximumRangeRule(
    var min: Double,
    var max: Double,
) : CappedScorecardScheduleRule {
    override fun check(item: CappedScorecardSchedule): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped scorecard schedule. */
data class CappedScorecardScheduleValidator(var rules: MutableList<CappedScorecardScheduleRule>) {
    fun validate(item: CappedScorecardSchedule): MutableList<String> {
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

    fun requireValid(item: CappedScorecardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedScorecardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedScorecardScheduleValidator(): CappedScorecardScheduleValidator {
    return CappedScorecardScheduleValidator(
        mutableListOf<CappedScorecardScheduleRule>(
            CappedScorecardScheduleCodePrefixRule("ACM"),
            CappedScorecardScheduleRequiredRouteCodeRule("capped scorecard schedule needs a value"),
            CappedScorecardScheduleRequiredChannelRule("capped scorecard schedule needs an entry"),
            CappedScorecardScheduleCodeLengthRule(3600),
            CappedScorecardScheduleQuantityRangeRule(255, 1000),
            CappedScorecardScheduleMaximumRangeRule(3.5, 3412.5),
        ),
    )
}
