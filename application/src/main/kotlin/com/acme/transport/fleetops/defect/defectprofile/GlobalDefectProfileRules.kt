package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize

/** A rule a global defect profile must pass. Check returns its problems, or none. */
interface GlobalDefectProfileRule {
    fun check(item: GlobalDefectProfile): MutableList<String>
}

data class GlobalDefectProfileRequiredContactEmailRule(var message: String) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectProfilePriorityRangeRule(var min: Int, var max: Int) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class GlobalDefectProfileCodePrefixRule(var prefix: String) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectProfileAllowedTrailerIdRule(var allowed: MutableList<String>) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectProfileRevisionRangeRule(var min: Int, var max: Int) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectProfileEndsAtRangeRule(var min: Int, var max: Int) : GlobalDefectProfileRule {
    override fun check(item: GlobalDefectProfile): MutableList<String> {
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

/** Runs every rule of a global defect profile. */
data class GlobalDefectProfileValidator(var rules: MutableList<GlobalDefectProfileRule>) {
    fun validate(item: GlobalDefectProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDefectProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDefectProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDefectProfileValidator(): GlobalDefectProfileValidator {
    return GlobalDefectProfileValidator(
        mutableListOf<GlobalDefectProfileRule>(
            GlobalDefectProfileRequiredContactEmailRule("global defect profile needs an entry"),
            GlobalDefectProfilePriorityRangeRule(80, 120),
            GlobalDefectProfileCodePrefixRule("ACM"),
            GlobalDefectProfileAllowedTrailerIdRule(mutableListOf<String>()),
            GlobalDefectProfileRevisionRangeRule(16, 1000),
            GlobalDefectProfileEndsAtRangeRule(80, 255),
        ),
    )
}
