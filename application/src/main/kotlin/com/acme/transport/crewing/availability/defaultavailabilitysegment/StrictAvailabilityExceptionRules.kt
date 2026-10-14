package com.acme.transport.crewing.availability.defaultavailabilitysegment

/** A rule a strict availability exception must pass. Check returns its problems, or none. */
interface StrictAvailabilityExceptionRule {
    fun check(item: StrictAvailabilityException): MutableList<String>
}

data class StrictAvailabilityExceptionRequiredTimezoneRule(
    var message: String,
) : StrictAvailabilityExceptionRule {
    override fun check(item: StrictAvailabilityException): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityExceptionRequiredShiftCodeRule(
    var message: String,
) : StrictAvailabilityExceptionRule {
    override fun check(item: StrictAvailabilityException): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityExceptionExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictAvailabilityExceptionRule {
    override fun check(item: StrictAvailabilityException): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityExceptionRequiredCodeRule(var message: String) : StrictAvailabilityExceptionRule {
    override fun check(item: StrictAvailabilityException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict availability exception. */
data class StrictAvailabilityExceptionValidator(var rules: MutableList<StrictAvailabilityExceptionRule>) {
    fun validate(item: StrictAvailabilityException): MutableList<String> {
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

    fun requireValid(item: StrictAvailabilityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAvailabilityException>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAvailabilityExceptionValidator(): StrictAvailabilityExceptionValidator {
    return StrictAvailabilityExceptionValidator(
        mutableListOf<StrictAvailabilityExceptionRule>(
            StrictAvailabilityExceptionRequiredTimezoneRule("strict availability exception needs an entry"),
            StrictAvailabilityExceptionRequiredShiftCodeRule("strict availability exception needs this field"),
            StrictAvailabilityExceptionExpiresAtRangeRule(3600, 86400),
            StrictAvailabilityExceptionRequiredCodeRule("strict availability exception needs an entry"),
        ),
    )
}
