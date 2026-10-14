package com.acme.transport.terminal.appointment.group

/** A rule a fallback appointment group must pass. Check returns its problems, or none. */
interface FallbackAppointmentGroupRule {
    fun check(item: FallbackAppointmentGroup): MutableList<String>
}

data class FallbackAppointmentGroupRequiredCodeRule(var message: String) : FallbackAppointmentGroupRule {
    override fun check(item: FallbackAppointmentGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentGroupCodePrefixRule(var prefix: String) : FallbackAppointmentGroupRule {
    override fun check(item: FallbackAppointmentGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentGroupUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentGroupRule {
    override fun check(item: FallbackAppointmentGroup): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class FallbackAppointmentGroupFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FallbackAppointmentGroupRule {
    override fun check(item: FallbackAppointmentGroup): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class FallbackAppointmentGroupRequiredLabelRule(var message: String) : FallbackAppointmentGroupRule {
    override fun check(item: FallbackAppointmentGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment group. */
data class FallbackAppointmentGroupValidator(var rules: MutableList<FallbackAppointmentGroupRule>) {
    fun validate(item: FallbackAppointmentGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAppointmentGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentGroupValidator(): FallbackAppointmentGroupValidator {
    return FallbackAppointmentGroupValidator(
        mutableListOf<FallbackAppointmentGroupRule>(
            FallbackAppointmentGroupRequiredCodeRule("fallback appointment group needs an entry"),
            FallbackAppointmentGroupCodePrefixRule("REF"),
            FallbackAppointmentGroupUpdatedAtRangeRule(80, 120),
            FallbackAppointmentGroupFloorValueRangeRule(0.6, 1513.3),
            FallbackAppointmentGroupRequiredLabelRule("fallback appointment group needs an entry"),
        ),
    )
}
