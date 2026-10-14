package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.text.normalize

/** A rule a adaptive backhaul target must pass. Check returns its problems, or none. */
interface AdaptiveBackhaulTargetRule {
    fun check(item: AdaptiveBackhaulTarget): MutableList<String>
}

data class AdaptiveBackhaulTargetAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulTargetRequiredLabelRule(var message: String) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulTargetSequenceRangeRule(var min: Int, var max: Int) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulTargetStopCountRangeRule(var min: Int, var max: Int) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulTargetCodePrefixRule(var prefix: String) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulTargetRequiredLocaleRule(var message: String) : AdaptiveBackhaulTargetRule {
    override fun check(item: AdaptiveBackhaulTarget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive backhaul target. */
data class AdaptiveBackhaulTargetValidator(var rules: MutableList<AdaptiveBackhaulTargetRule>) {
    fun validate(item: AdaptiveBackhaulTarget): MutableList<String> {
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

    fun requireValid(item: AdaptiveBackhaulTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBackhaulTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBackhaulTargetValidator(): AdaptiveBackhaulTargetValidator {
    return AdaptiveBackhaulTargetValidator(
        mutableListOf<AdaptiveBackhaulTargetRule>(
            AdaptiveBackhaulTargetAllowedLabelRule(mutableListOf<String>()),
            AdaptiveBackhaulTargetRequiredLabelRule("adaptive backhaul target needs an entry"),
            AdaptiveBackhaulTargetSequenceRangeRule(32, 1000),
            AdaptiveBackhaulTargetStopCountRangeRule(80, 80),
            AdaptiveBackhaulTargetCodePrefixRule("Q"),
            AdaptiveBackhaulTargetRequiredLocaleRule("adaptive backhaul target needs this field"),
        ),
    )
}
