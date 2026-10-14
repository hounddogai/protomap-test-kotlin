package com.acme.transport.terminal.appointment.summary

/** A rule a fallback appointment baseline must pass. Check returns its problems, or none. */
interface FallbackAppointmentBaselineRule {
    fun check(item: FallbackAppointmentBaseline): MutableList<String>
}

data class FallbackAppointmentBaselineCodePrefixRule(var prefix: String) : FallbackAppointmentBaselineRule {
    override fun check(item: FallbackAppointmentBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBaselinePinnedGuardRule(var limit: Int) : FallbackAppointmentBaselineRule {
    override fun check(item: FallbackAppointmentBaseline): MutableList<String> {
        if (item.pinned && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentBaselineEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentBaselineRule {
    override fun check(item: FallbackAppointmentBaseline): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class FallbackAppointmentBaselineEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentBaselineRule {
    override fun check(item: FallbackAppointmentBaseline): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class FallbackAppointmentBaselineRequiredCodeRule(var message: String) : FallbackAppointmentBaselineRule {
    override fun check(item: FallbackAppointmentBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment baseline. */
data class FallbackAppointmentBaselineValidator(var rules: MutableList<FallbackAppointmentBaselineRule>) {
    fun validate(item: FallbackAppointmentBaseline): MutableList<String> {
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

    fun requireValid(item: FallbackAppointmentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentBaselineValidator(): FallbackAppointmentBaselineValidator {
    return FallbackAppointmentBaselineValidator(
        mutableListOf<FallbackAppointmentBaselineRule>(
            FallbackAppointmentBaselineCodePrefixRule("OPS"),
            FallbackAppointmentBaselinePinnedGuardRule(255),
            FallbackAppointmentBaselineEndsAtRangeRule(80, 255),
            FallbackAppointmentBaselineEtaMinutesRangeRule(80, 255),
            FallbackAppointmentBaselineRequiredCodeRule("fallback appointment baseline needs this field"),
        ),
    )
}
