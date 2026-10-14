package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize

/** A rule a redelivery variance must pass. Check returns its problems, or none. */
interface RedeliveryVarianceRule {
    fun check(item: RedeliveryVariance): MutableList<String>
}

data class RedeliveryVarianceAllowedCodeRule(var allowed: MutableList<String>) : RedeliveryVarianceRule {
    override fun check(item: RedeliveryVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryVarianceRequiredTitleRule(var message: String) : RedeliveryVarianceRule {
    override fun check(item: RedeliveryVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryVarianceRequiredCodeRule(var message: String) : RedeliveryVarianceRule {
    override fun check(item: RedeliveryVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery variance. */
data class RedeliveryVarianceValidator(var rules: MutableList<RedeliveryVarianceRule>) {
    fun validate(item: RedeliveryVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryVarianceValidator(): RedeliveryVarianceValidator {
    return RedeliveryVarianceValidator(
        mutableListOf<RedeliveryVarianceRule>(
            RedeliveryVarianceAllowedCodeRule(mutableListOf<String>()),
            RedeliveryVarianceRequiredTitleRule("redelivery variance needs this field"),
            RedeliveryVarianceRequiredCodeRule("redelivery variance needs this field"),
        ),
    )
}
