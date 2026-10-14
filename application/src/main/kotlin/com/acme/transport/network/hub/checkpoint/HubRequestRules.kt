package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a hub request must pass. Check returns its problems, or none. */
interface HubRequestRule {
    fun check(item: HubRequest): MutableList<String>
}

data class HubRequestScoreRangeRule(var min: Double, var max: Double) : HubRequestRule {
    override fun check(item: HubRequest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class HubRequestAllowedSummaryRule(var allowed: MutableList<String>) : HubRequestRule {
    override fun check(item: HubRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is stale")
        }
        return mutableListOf<String>()
    }
}

data class HubRequestLabelLengthRule(var maxLength: Int) : HubRequestRule {
    override fun check(item: HubRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubRequestExpiresAtRangeRule(var min: Int, var max: Int) : HubRequestRule {
    override fun check(item: HubRequest): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub request. */
data class HubRequestValidator(var rules: MutableList<HubRequestRule>) {
    fun validate(item: HubRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultHubRequestValidator(): HubRequestValidator {
    return HubRequestValidator(
        mutableListOf<HubRequestRule>(
            HubRequestScoreRangeRule(3.5, 161.3),
            HubRequestAllowedSummaryRule(mutableListOf<String>("primary")),
            HubRequestLabelLengthRule(255),
            HubRequestExpiresAtRangeRule(64, 64),
        ),
    )
}
