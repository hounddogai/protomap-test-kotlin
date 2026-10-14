package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.normalize

/** A rule a availability estimate must pass. Check returns its problems, or none. */
interface AvailabilityEstimateRule {
    fun check(item: AvailabilityEstimate): MutableList<String>
}

data class AvailabilityEstimateCodeLengthRule(var maxLength: Int) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityEstimateAllowedCodeRule(var allowed: MutableList<String>) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityEstimateRequiredTimezoneRule(var message: String) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityEstimateRequiredTitleRule(var message: String) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityEstimateTimezoneLengthRule(var maxLength: Int) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityEstimateRequiredCodeRule(var message: String) : AvailabilityEstimateRule {
    override fun check(item: AvailabilityEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability estimate. */
data class AvailabilityEstimateValidator(var rules: MutableList<AvailabilityEstimateRule>) {
    fun validate(item: AvailabilityEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityEstimateValidator(): AvailabilityEstimateValidator {
    return AvailabilityEstimateValidator(
        mutableListOf<AvailabilityEstimateRule>(
            AvailabilityEstimateCodeLengthRule(16),
            AvailabilityEstimateAllowedCodeRule(mutableListOf<String>()),
            AvailabilityEstimateRequiredTimezoneRule("availability estimate needs this field"),
            AvailabilityEstimateRequiredTitleRule("availability estimate needs this field"),
            AvailabilityEstimateTimezoneLengthRule(255),
            AvailabilityEstimateRequiredCodeRule("availability estimate needs an entry"),
        ),
    )
}
