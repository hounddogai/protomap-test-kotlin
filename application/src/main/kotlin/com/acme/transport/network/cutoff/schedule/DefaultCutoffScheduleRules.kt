package com.acme.transport.network.cutoff.schedule

/** A rule a default cutoff schedule must pass. Check returns its problems, or none. */
interface DefaultCutoffScheduleRule {
    fun check(item: DefaultCutoffSchedule): MutableList<String>
}

data class DefaultCutoffScheduleRatioRangeRule(var min: Double, var max: Double) : DefaultCutoffScheduleRule {
    override fun check(item: DefaultCutoffSchedule): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffScheduleCeilingRangeRule(var min: Double, var max: Double) : DefaultCutoffScheduleRule {
    override fun check(item: DefaultCutoffSchedule): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffSchedulePalletsRangeRule(var min: Int, var max: Int) : DefaultCutoffScheduleRule {
    override fun check(item: DefaultCutoffSchedule): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffScheduleShiftCodeLengthRule(var maxLength: Int) : DefaultCutoffScheduleRule {
    override fun check(item: DefaultCutoffSchedule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff schedule. */
data class DefaultCutoffScheduleValidator(var rules: MutableList<DefaultCutoffScheduleRule>) {
    fun validate(item: DefaultCutoffSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCutoffSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffScheduleValidator(): DefaultCutoffScheduleValidator {
    return DefaultCutoffScheduleValidator(
        mutableListOf<DefaultCutoffScheduleRule>(
            DefaultCutoffScheduleRatioRangeRule(6.4, 4331.0),
            DefaultCutoffScheduleCeilingRangeRule(8.8, 4677.7),
            DefaultCutoffSchedulePalletsRangeRule(80, 120),
            DefaultCutoffScheduleShiftCodeLengthRule(80),
        ),
    )
}
