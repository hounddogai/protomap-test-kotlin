package com.acme.transport.linehaul.relay.threshold

/** A rule a capped relay schedule must pass. Check returns its problems, or none. */
interface CappedRelayScheduleRule {
    fun check(item: CappedRelaySchedule): MutableList<String>
}

data class CappedRelayScheduleRegionLengthRule(var maxLength: Int) : CappedRelayScheduleRule {
    override fun check(item: CappedRelaySchedule): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayScheduleCountryLengthRule(var maxLength: Int) : CappedRelayScheduleRule {
    override fun check(item: CappedRelaySchedule): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayScheduleMaximumRangeRule(var min: Double, var max: Double) : CappedRelayScheduleRule {
    override fun check(item: CappedRelaySchedule): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRelayScheduleRequiredRegionRule(var message: String) : CappedRelayScheduleRule {
    override fun check(item: CappedRelaySchedule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped relay schedule. */
data class CappedRelayScheduleValidator(var rules: MutableList<CappedRelayScheduleRule>) {
    fun validate(item: CappedRelaySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedRelaySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRelaySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRelayScheduleValidator(): CappedRelayScheduleValidator {
    return CappedRelayScheduleValidator(
        mutableListOf<CappedRelayScheduleRule>(
            CappedRelayScheduleRegionLengthRule(255),
            CappedRelayScheduleCountryLengthRule(120),
            CappedRelayScheduleMaximumRangeRule(2.2, 4557.3),
            CappedRelayScheduleRequiredRegionRule("capped relay schedule needs this field"),
        ),
    )
}
