package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard override must pass. Check returns its problems, or none. */
interface TrailerYardOverrideRule {
    fun check(item: TrailerYardOverride): MutableList<String>
}

data class TrailerYardOverrideAllowedCarrierRule(var allowed: MutableList<String>) : TrailerYardOverrideRule {
    override fun check(item: TrailerYardOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardOverrideRequiredNameRule(var message: String) : TrailerYardOverrideRule {
    override fun check(item: TrailerYardOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardOverrideRequiredCategoryRule(var message: String) : TrailerYardOverrideRule {
    override fun check(item: TrailerYardOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard override. */
data class TrailerYardOverrideValidator(var rules: MutableList<TrailerYardOverrideRule>) {
    fun validate(item: TrailerYardOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardOverrideValidator(): TrailerYardOverrideValidator {
    return TrailerYardOverrideValidator(
        mutableListOf<TrailerYardOverrideRule>(
            TrailerYardOverrideAllowedCarrierRule(mutableListOf<String>("batched", "weighted", "scheduled")),
            TrailerYardOverrideRequiredNameRule("trailer yard override needs this field"),
            TrailerYardOverrideRequiredCategoryRule("trailer yard override needs an entry"),
        ),
    )
}
