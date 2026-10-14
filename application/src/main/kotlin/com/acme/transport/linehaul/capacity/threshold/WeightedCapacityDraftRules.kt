package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize

/** A rule a weighted capacity draft must pass. Check returns its problems, or none. */
interface WeightedCapacityDraftRule {
    fun check(item: WeightedCapacityDraft): MutableList<String>
}

data class WeightedCapacityDraftAllowedTitleRule(var allowed: MutableList<String>) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityDraftRequiredTitleRule(var message: String) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityDraftCodeLengthRule(var maxLength: Int) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityDraftTitleLengthRule(var maxLength: Int) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityDraftEtaMinutesRangeRule(var min: Int, var max: Int) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityDraftCodePrefixRule(var prefix: String) : WeightedCapacityDraftRule {
    override fun check(item: WeightedCapacityDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted capacity draft. */
data class WeightedCapacityDraftValidator(var rules: MutableList<WeightedCapacityDraftRule>) {
    fun validate(item: WeightedCapacityDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCapacityDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCapacityDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCapacityDraftValidator(): WeightedCapacityDraftValidator {
    return WeightedCapacityDraftValidator(
        mutableListOf<WeightedCapacityDraftRule>(
            WeightedCapacityDraftAllowedTitleRule(mutableListOf<String>("primary", "batched", "tiered")),
            WeightedCapacityDraftRequiredTitleRule("weighted capacity draft needs this field"),
            WeightedCapacityDraftCodeLengthRule(32),
            WeightedCapacityDraftTitleLengthRule(80),
            WeightedCapacityDraftEtaMinutesRangeRule(32, 64),
            WeightedCapacityDraftCodePrefixRule("ACM"),
        ),
    )
}
