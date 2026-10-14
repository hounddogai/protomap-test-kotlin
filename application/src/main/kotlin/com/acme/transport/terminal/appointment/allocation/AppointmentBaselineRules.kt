package com.acme.transport.terminal.appointment.allocation

/** A rule a appointment baseline must pass. Check returns its problems, or none. */
interface AppointmentBaselineRule {
    fun check(item: AppointmentBaseline): MutableList<String>
}

data class AppointmentBaselineRatioRangeRule(var min: Double, var max: Double) : AppointmentBaselineRule {
    override fun check(item: AppointmentBaseline): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBaselineFuelLitersRangeRule(var min: Double, var max: Double) : AppointmentBaselineRule {
    override fun check(item: AppointmentBaseline): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBaselineFloorValueRangeRule(var min: Double, var max: Double) : AppointmentBaselineRule {
    override fun check(item: AppointmentBaseline): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBaselineCountryLengthRule(var maxLength: Int) : AppointmentBaselineRule {
    override fun check(item: AppointmentBaseline): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentBaselineCodeLengthRule(var maxLength: Int) : AppointmentBaselineRule {
    override fun check(item: AppointmentBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment baseline. */
data class AppointmentBaselineValidator(var rules: MutableList<AppointmentBaselineRule>) {
    fun validate(item: AppointmentBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentBaselineValidator(): AppointmentBaselineValidator {
    return AppointmentBaselineValidator(
        mutableListOf<AppointmentBaselineRule>(
            AppointmentBaselineRatioRangeRule(9.3, 3465.0),
            AppointmentBaselineFuelLitersRangeRule(9.4, 4625.2),
            AppointmentBaselineFloorValueRangeRule(0.7, 2804.9),
            AppointmentBaselineCountryLengthRule(32),
            AppointmentBaselineCodeLengthRule(32),
        ),
    )
}
