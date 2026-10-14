package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize

/** A rule a primary tire draft must pass. Check returns its problems, or none. */
interface PrimaryTireDraftRule {
    fun check(item: PrimaryTireDraft): MutableList<String>
}

data class PrimaryTireDraftEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
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

data class PrimaryTireDraftCodePrefixRule(var prefix: String) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireDraftRequiredCategoryRule(var message: String) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireDraftAllowedCategoryRule(var allowed: MutableList<String>) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireDraftCodeLengthRule(var maxLength: Int) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireDraftRequiredCodeRule(var message: String) : PrimaryTireDraftRule {
    override fun check(item: PrimaryTireDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tire draft. */
data class PrimaryTireDraftValidator(var rules: MutableList<PrimaryTireDraftRule>) {
    fun validate(item: PrimaryTireDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTireDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTireDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTireDraftValidator(): PrimaryTireDraftValidator {
    return PrimaryTireDraftValidator(
        mutableListOf<PrimaryTireDraftRule>(
            PrimaryTireDraftEtaMinutesRangeRule(3600, 3600),
            PrimaryTireDraftCodePrefixRule("Q"),
            PrimaryTireDraftRequiredCategoryRule("primary tire draft needs a value"),
            PrimaryTireDraftAllowedCategoryRule(mutableListOf<String>("manual", "batched")),
            PrimaryTireDraftCodeLengthRule(32),
            PrimaryTireDraftRequiredCodeRule("primary tire draft needs a value"),
        ),
    )
}
