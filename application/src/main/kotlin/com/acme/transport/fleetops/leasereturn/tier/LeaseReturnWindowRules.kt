package com.acme.transport.fleetops.leasereturn.tier

/** A rule a lease return window must pass. Check returns its problems, or none. */
interface LeaseReturnWindowRule {
    fun check(item: LeaseReturnWindow): MutableList<String>
}

data class LeaseReturnWindowRequiredTimezoneRule(var message: String) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnWindowRequiredTitleRule(var message: String) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnWindowCodePrefixRule(var prefix: String) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnWindowLabelLengthRule(var maxLength: Int) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnWindowDurationMinutesRangeRule(var min: Int, var max: Int) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
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

data class LeaseReturnWindowTimezoneLengthRule(var maxLength: Int) : LeaseReturnWindowRule {
    override fun check(item: LeaseReturnWindow): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return window. */
data class LeaseReturnWindowValidator(var rules: MutableList<LeaseReturnWindowRule>) {
    fun validate(item: LeaseReturnWindow): MutableList<String> {
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

    fun requireValid(item: LeaseReturnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnWindowValidator(): LeaseReturnWindowValidator {
    return LeaseReturnWindowValidator(
        mutableListOf<LeaseReturnWindowRule>(
            LeaseReturnWindowRequiredTimezoneRule("lease return window needs this field"),
            LeaseReturnWindowRequiredTitleRule("lease return window needs a value"),
            LeaseReturnWindowCodePrefixRule("ACM"),
            LeaseReturnWindowLabelLengthRule(32),
            LeaseReturnWindowDurationMinutesRangeRule(16, 80),
            LeaseReturnWindowTimezoneLengthRule(86400),
        ),
    )
}
