package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize

/** A rule a regional availability plan must pass. Check returns its problems, or none. */
interface RegionalAvailabilityPlanRule {
    fun check(item: RegionalAvailabilityPlan): MutableList<String>
}

data class RegionalAvailabilityPlanAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalAvailabilityPlanRule {
    override fun check(item: RegionalAvailabilityPlan): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityPlanAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : RegionalAvailabilityPlanRule {
    override fun check(item: RegionalAvailabilityPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityPlanContactEmailLengthRule(var maxLength: Int) : RegionalAvailabilityPlanRule {
    override fun check(item: RegionalAvailabilityPlan): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityPlanRequiredNameRule(var message: String) : RegionalAvailabilityPlanRule {
    override fun check(item: RegionalAvailabilityPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional availability plan. */
data class RegionalAvailabilityPlanValidator(var rules: MutableList<RegionalAvailabilityPlanRule>) {
    fun validate(item: RegionalAvailabilityPlan): MutableList<String> {
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

    fun requireValid(item: RegionalAvailabilityPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAvailabilityPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAvailabilityPlanValidator(): RegionalAvailabilityPlanValidator {
    return RegionalAvailabilityPlanValidator(
        mutableListOf<RegionalAvailabilityPlanRule>(
            RegionalAvailabilityPlanAttemptsRangeRule(80, 120),
            RegionalAvailabilityPlanAllowedTimezoneRule(mutableListOf<String>("legacy")),
            RegionalAvailabilityPlanContactEmailLengthRule(32),
            RegionalAvailabilityPlanRequiredNameRule("regional availability plan needs an entry"),
        ),
    )
}
