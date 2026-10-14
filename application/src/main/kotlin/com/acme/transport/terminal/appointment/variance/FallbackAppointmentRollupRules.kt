package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize

/** A rule a fallback appointment rollup must pass. Check returns its problems, or none. */
interface FallbackAppointmentRollupRule {
    fun check(item: FallbackAppointmentRollup): MutableList<String>
}

data class FallbackAppointmentRollupAllowedNameRule(
    var allowed: MutableList<String>,
) : FallbackAppointmentRollupRule {
    override fun check(item: FallbackAppointmentRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentRollupTrailerIdLengthRule(var maxLength: Int) : FallbackAppointmentRollupRule {
    override fun check(item: FallbackAppointmentRollup): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentRollupRequiredTrailerIdRule(var message: String) : FallbackAppointmentRollupRule {
    override fun check(item: FallbackAppointmentRollup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAppointmentRollupCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackAppointmentRollupRule {
    override fun check(item: FallbackAppointmentRollup): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback appointment rollup. */
data class FallbackAppointmentRollupValidator(var rules: MutableList<FallbackAppointmentRollupRule>) {
    fun validate(item: FallbackAppointmentRollup): MutableList<String> {
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

    fun requireValid(item: FallbackAppointmentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAppointmentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAppointmentRollupValidator(): FallbackAppointmentRollupValidator {
    return FallbackAppointmentRollupValidator(
        mutableListOf<FallbackAppointmentRollupRule>(
            FallbackAppointmentRollupAllowedNameRule(mutableListOf<String>("strict", "fallback", "tiered")),
            FallbackAppointmentRollupTrailerIdLengthRule(86400),
            FallbackAppointmentRollupRequiredTrailerIdRule("fallback appointment rollup needs a value"),
            FallbackAppointmentRollupCreatedAtRangeRule(32, 255),
        ),
    )
}
