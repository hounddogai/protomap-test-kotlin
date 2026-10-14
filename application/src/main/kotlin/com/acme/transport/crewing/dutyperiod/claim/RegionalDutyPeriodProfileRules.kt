package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize

/** A rule a regional duty period profile must pass. Check returns its problems, or none. */
interface RegionalDutyPeriodProfileRule {
    fun check(item: RegionalDutyPeriodProfile): MutableList<String>
}

data class RegionalDutyPeriodProfileAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : RegionalDutyPeriodProfileRule {
    override fun check(item: RegionalDutyPeriodProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodProfileExternalRefLengthRule(var maxLength: Int) : RegionalDutyPeriodProfileRule {
    override fun check(item: RegionalDutyPeriodProfile): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodProfileLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalDutyPeriodProfileRule {
    override fun check(item: RegionalDutyPeriodProfile): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodProfileNameLengthRule(var maxLength: Int) : RegionalDutyPeriodProfileRule {
    override fun check(item: RegionalDutyPeriodProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodProfileRequiredCodeRule(var message: String) : RegionalDutyPeriodProfileRule {
    override fun check(item: RegionalDutyPeriodProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional duty period profile. */
data class RegionalDutyPeriodProfileValidator(var rules: MutableList<RegionalDutyPeriodProfileRule>) {
    fun validate(item: RegionalDutyPeriodProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDutyPeriodProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDutyPeriodProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDutyPeriodProfileValidator(): RegionalDutyPeriodProfileValidator {
    return RegionalDutyPeriodProfileValidator(
        mutableListOf<RegionalDutyPeriodProfileRule>(
            RegionalDutyPeriodProfileAllowedExternalRefRule(
                mutableListOf<String>("lenient", "batched", "weighted"),
            ),
            RegionalDutyPeriodProfileExternalRefLengthRule(32),
            RegionalDutyPeriodProfileLoadKgRangeRule(4.1, 3238.9),
            RegionalDutyPeriodProfileNameLengthRule(86400),
            RegionalDutyPeriodProfileRequiredCodeRule("regional duty period profile needs a value"),
        ),
    )
}
