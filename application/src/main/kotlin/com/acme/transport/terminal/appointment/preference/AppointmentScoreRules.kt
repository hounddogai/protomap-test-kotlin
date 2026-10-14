package com.acme.transport.terminal.appointment.preference

/** A rule a appointment score must pass. Check returns its problems, or none. */
interface AppointmentScoreRule {
    fun check(item: AppointmentScore): MutableList<String>
}

data class AppointmentScoreCodeLengthRule(var maxLength: Int) : AppointmentScoreRule {
    override fun check(item: AppointmentScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentScoreScoreRangeRule(var min: Double, var max: Double) : AppointmentScoreRule {
    override fun check(item: AppointmentScore): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class AppointmentScoreCodePrefixRule(var prefix: String) : AppointmentScoreRule {
    override fun check(item: AppointmentScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentScoreNameLengthRule(var maxLength: Int) : AppointmentScoreRule {
    override fun check(item: AppointmentScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentScoreCostRangeRule(var min: Double, var max: Double) : AppointmentScoreRule {
    override fun check(item: AppointmentScore): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a appointment score. */
data class AppointmentScoreValidator(var rules: MutableList<AppointmentScoreRule>) {
    fun validate(item: AppointmentScore): MutableList<String> {
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

    fun requireValid(item: AppointmentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentScoreValidator(): AppointmentScoreValidator {
    return AppointmentScoreValidator(
        mutableListOf<AppointmentScoreRule>(
            AppointmentScoreCodeLengthRule(86400),
            AppointmentScoreScoreRangeRule(3.4, 2459.1),
            AppointmentScoreCodePrefixRule("REF"),
            AppointmentScoreNameLengthRule(1000),
            AppointmentScoreCostRangeRule(9.3, 897.7),
        ),
    )
}
