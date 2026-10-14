package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

/** A rule a manual parcel mapping must pass. Check returns its problems, or none. */
interface ManualParcelMappingRule {
    fun check(item: ManualParcelMapping): MutableList<String>
}

data class ManualParcelMappingCodePrefixRule(var prefix: String) : ManualParcelMappingRule {
    override fun check(item: ManualParcelMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMappingWindowDaysRangeRule(var min: Int, var max: Int) : ManualParcelMappingRule {
    override fun check(item: ManualParcelMapping): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMappingDurationMinutesRangeRule(var min: Int, var max: Int) : ManualParcelMappingRule {
    override fun check(item: ManualParcelMapping): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMappingRequiredCountryRule(var message: String) : ManualParcelMappingRule {
    override fun check(item: ManualParcelMapping): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMappingApprovedGuardRule(var limit: Int) : ManualParcelMappingRule {
    override fun check(item: ManualParcelMapping): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual parcel mapping. */
data class ManualParcelMappingValidator(var rules: MutableList<ManualParcelMappingRule>) {
    fun validate(item: ManualParcelMapping): MutableList<String> {
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

    fun requireValid(item: ManualParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultManualParcelMappingValidator(): ManualParcelMappingValidator {
    return ManualParcelMappingValidator(
        mutableListOf<ManualParcelMappingRule>(
            ManualParcelMappingCodePrefixRule("X"),
            ManualParcelMappingWindowDaysRangeRule(255, 1000),
            ManualParcelMappingDurationMinutesRangeRule(16, 1000),
            ManualParcelMappingRequiredCountryRule("manual parcel mapping needs this field"),
            ManualParcelMappingApprovedGuardRule(64),
        ),
    )
}
