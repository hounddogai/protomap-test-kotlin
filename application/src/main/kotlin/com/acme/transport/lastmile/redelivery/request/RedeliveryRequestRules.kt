package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize

/** A rule a redelivery request must pass. Check returns its problems, or none. */
interface RedeliveryRequestRule {
    fun check(item: RedeliveryRequest): MutableList<String>
}

data class RedeliveryRequestRetriesRangeRule(var min: Int, var max: Int) : RedeliveryRequestRule {
    override fun check(item: RedeliveryRequest): MutableList<String> {
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

data class RedeliveryRequestAllowedTitleRule(var allowed: MutableList<String>) : RedeliveryRequestRule {
    override fun check(item: RedeliveryRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryRequestRequiredCategoryRule(var message: String) : RedeliveryRequestRule {
    override fun check(item: RedeliveryRequest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery request. */
data class RedeliveryRequestValidator(var rules: MutableList<RedeliveryRequestRule>) {
    fun validate(item: RedeliveryRequest): MutableList<String> {
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

    fun requireValid(item: RedeliveryRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryRequestValidator(): RedeliveryRequestValidator {
    return RedeliveryRequestValidator(
        mutableListOf<RedeliveryRequestRule>(
            RedeliveryRequestRetriesRangeRule(1000, 86400),
            RedeliveryRequestAllowedTitleRule(mutableListOf<String>()),
            RedeliveryRequestRequiredCategoryRule("redelivery request needs a value"),
        ),
    )
}
