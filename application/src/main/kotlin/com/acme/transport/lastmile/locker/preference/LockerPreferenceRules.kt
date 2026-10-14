package com.acme.transport.lastmile.locker.preference

/** A rule a locker preference must pass. Check returns its problems, or none. */
interface LockerPreferenceRule {
    fun check(item: LockerPreference): MutableList<String>
}

data class LockerPreferenceCodePrefixRule(var prefix: String) : LockerPreferenceRule {
    override fun check(item: LockerPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerPreferenceCeilingRangeRule(var min: Double, var max: Double) : LockerPreferenceRule {
    override fun check(item: LockerPreference): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerPreferenceRequiredNameRule(var message: String) : LockerPreferenceRule {
    override fun check(item: LockerPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerPreferenceDurationMinutesRangeRule(var min: Int, var max: Int) : LockerPreferenceRule {
    override fun check(item: LockerPreference): MutableList<String> {
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

data class LockerPreferenceRequiredRegionRule(var message: String) : LockerPreferenceRule {
    override fun check(item: LockerPreference): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker preference. */
data class LockerPreferenceValidator(var rules: MutableList<LockerPreferenceRule>) {
    fun validate(item: LockerPreference): MutableList<String> {
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

    fun requireValid(item: LockerPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerPreferenceValidator(): LockerPreferenceValidator {
    return LockerPreferenceValidator(
        mutableListOf<LockerPreferenceRule>(
            LockerPreferenceCodePrefixRule("ACM"),
            LockerPreferenceCeilingRangeRule(3.0, 3336.9),
            LockerPreferenceRequiredNameRule("locker preference needs a value"),
            LockerPreferenceDurationMinutesRangeRule(16, 120),
            LockerPreferenceRequiredRegionRule("locker preference needs this field"),
        ),
    )
}
