package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.normalize

/** A rule a strict coaching entry must pass. Check returns its problems, or none. */
interface StrictCoachingEntryRule {
    fun check(item: StrictCoachingEntry): MutableList<String>
}

data class StrictCoachingEntryWindowDaysRangeRule(var min: Int, var max: Int) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingEntryRequiredCodeRule(var message: String) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingEntryRetriesRangeRule(var min: Int, var max: Int) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class StrictCoachingEntryQuantityRangeRule(var min: Int, var max: Int) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingEntryAllowedNameRule(var allowed: MutableList<String>) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingEntryCodeLengthRule(var maxLength: Int) : StrictCoachingEntryRule {
    override fun check(item: StrictCoachingEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict coaching entry. */
data class StrictCoachingEntryValidator(var rules: MutableList<StrictCoachingEntryRule>) {
    fun validate(item: StrictCoachingEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictCoachingEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCoachingEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCoachingEntryValidator(): StrictCoachingEntryValidator {
    return StrictCoachingEntryValidator(
        mutableListOf<StrictCoachingEntryRule>(
            StrictCoachingEntryWindowDaysRangeRule(255, 3600),
            StrictCoachingEntryRequiredCodeRule("strict coaching entry needs this field"),
            StrictCoachingEntryRetriesRangeRule(80, 255),
            StrictCoachingEntryQuantityRangeRule(1000, 3600),
            StrictCoachingEntryAllowedNameRule(mutableListOf<String>("regional", "batched")),
            StrictCoachingEntryCodeLengthRule(32),
        ),
    )
}
