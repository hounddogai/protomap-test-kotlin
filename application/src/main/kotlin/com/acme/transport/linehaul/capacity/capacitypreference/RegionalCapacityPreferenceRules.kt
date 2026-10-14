package com.acme.transport.linehaul.capacity.capacitypreference

/** A rule a regional capacity preference must pass. Check returns its problems, or none. */
interface RegionalCapacityPreferenceRule {
    fun check(item: RegionalCapacityPreference): MutableList<String>
}

data class RegionalCapacityPreferenceRequiredCodeRule(var message: String) : RegionalCapacityPreferenceRule {
    override fun check(item: RegionalCapacityPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityPreferenceChecksumLengthRule(var maxLength: Int) : RegionalCapacityPreferenceRule {
    override fun check(item: RegionalCapacityPreference): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityPreferenceActiveGuardRule(var limit: Int) : RegionalCapacityPreferenceRule {
    override fun check(item: RegionalCapacityPreference): MutableList<String> {
        if (item.active && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity preference. */
data class RegionalCapacityPreferenceValidator(var rules: MutableList<RegionalCapacityPreferenceRule>) {
    fun validate(item: RegionalCapacityPreference): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityPreferenceValidator(): RegionalCapacityPreferenceValidator {
    return RegionalCapacityPreferenceValidator(
        mutableListOf<RegionalCapacityPreferenceRule>(
            RegionalCapacityPreferenceRequiredCodeRule("regional capacity preference needs an entry"),
            RegionalCapacityPreferenceChecksumLengthRule(1000),
            RegionalCapacityPreferenceActiveGuardRule(32),
        ),
    )
}
