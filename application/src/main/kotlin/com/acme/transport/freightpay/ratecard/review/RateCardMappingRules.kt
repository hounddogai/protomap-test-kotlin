package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.normalize

/** A rule a rate card mapping must pass. Check returns its problems, or none. */
interface RateCardMappingRule {
    fun check(item: RateCardMapping): MutableList<String>
}

data class RateCardMappingNameLengthRule(var maxLength: Int) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMappingAttemptsRangeRule(var min: Int, var max: Int) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class RateCardMappingVersionRangeRule(var min: Int, var max: Int) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RateCardMappingAllowedCategoryRule(var allowed: MutableList<String>) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMappingRequiredTimezoneRule(var message: String) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardMappingRequiredNameRule(var message: String) : RateCardMappingRule {
    override fun check(item: RateCardMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card mapping. */
data class RateCardMappingValidator(var rules: MutableList<RateCardMappingRule>) {
    fun validate(item: RateCardMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardMappingValidator(): RateCardMappingValidator {
    return RateCardMappingValidator(
        mutableListOf<RateCardMappingRule>(
            RateCardMappingNameLengthRule(32),
            RateCardMappingAttemptsRangeRule(64, 120),
            RateCardMappingVersionRangeRule(16, 16),
            RateCardMappingAllowedCategoryRule(mutableListOf<String>("pending")),
            RateCardMappingRequiredTimezoneRule("rate card mapping needs a value"),
            RateCardMappingRequiredNameRule("rate card mapping needs a value"),
        ),
    )
}
