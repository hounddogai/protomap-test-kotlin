package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize

/** A rule a trailer rule must pass. Check returns its problems, or none. */
interface TrailerRuleRule {
    fun check(item: TrailerRule): MutableList<String>
}

data class TrailerRuleSequenceRangeRule(var min: Int, var max: Int) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRuleRequiredLabelRule(var message: String) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerRuleCodeLengthRule(var maxLength: Int) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRuleCodePrefixRule(var prefix: String) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRuleAmountRangeRule(var min: Double, var max: Double) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerRuleAllowedLabelRule(var allowed: MutableList<String>) : TrailerRuleRule {
    override fun check(item: TrailerRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer rule. */
data class TrailerRuleValidator(var rules: MutableList<TrailerRuleRule>) {
    fun validate(item: TrailerRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerRuleValidator(): TrailerRuleValidator {
    return TrailerRuleValidator(
        mutableListOf<TrailerRuleRule>(
            TrailerRuleSequenceRangeRule(32, 1000),
            TrailerRuleRequiredLabelRule("trailer rule needs an entry"),
            TrailerRuleCodeLengthRule(1000),
            TrailerRuleCodePrefixRule("X"),
            TrailerRuleAmountRangeRule(2.5, 3463.8),
            TrailerRuleAllowedLabelRule(mutableListOf<String>("legacy")),
        ),
    )
}
