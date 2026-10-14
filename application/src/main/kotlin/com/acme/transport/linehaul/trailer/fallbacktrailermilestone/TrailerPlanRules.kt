package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.normalize

/** A rule a trailer plan must pass. Check returns its problems, or none. */
interface TrailerPlanRule {
    fun check(item: TrailerPlan): MutableList<String>
}

data class TrailerPlanAllowedTrailerIdRule(var allowed: MutableList<String>) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TrailerPlanTitleLengthRule(var maxLength: Int) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerPlanRequiredTitleRule(var message: String) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerPlanCodePrefixRule(var prefix: String) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerPlanLoadKgRangeRule(var min: Double, var max: Double) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerPlanCodeLengthRule(var maxLength: Int) : TrailerPlanRule {
    override fun check(item: TrailerPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer plan. */
data class TrailerPlanValidator(var rules: MutableList<TrailerPlanRule>) {
    fun validate(item: TrailerPlan): MutableList<String> {
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

    fun requireValid(item: TrailerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerPlanValidator(): TrailerPlanValidator {
    return TrailerPlanValidator(
        mutableListOf<TrailerPlanRule>(
            TrailerPlanAllowedTrailerIdRule(mutableListOf<String>("lenient", "seasonal", "batched")),
            TrailerPlanTitleLengthRule(255),
            TrailerPlanRequiredTitleRule("trailer plan needs this field"),
            TrailerPlanCodePrefixRule("Q"),
            TrailerPlanLoadKgRangeRule(6.2, 1823.8),
            TrailerPlanCodeLengthRule(32),
        ),
    )
}
