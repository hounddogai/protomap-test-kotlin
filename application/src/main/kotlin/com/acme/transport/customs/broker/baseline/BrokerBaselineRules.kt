package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.normalize

/** A rule a broker baseline must pass. Check returns its problems, or none. */
interface BrokerBaselineRule {
    fun check(item: BrokerBaseline): MutableList<String>
}

data class BrokerBaselineCodeLengthRule(var maxLength: Int) : BrokerBaselineRule {
    override fun check(item: BrokerBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerBaselineRequiredLabelRule(var message: String) : BrokerBaselineRule {
    override fun check(item: BrokerBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerBaselineAllowedTrailerIdRule(var allowed: MutableList<String>) : BrokerBaselineRule {
    override fun check(item: BrokerBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker baseline. */
data class BrokerBaselineValidator(var rules: MutableList<BrokerBaselineRule>) {
    fun validate(item: BrokerBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerBaselineValidator(): BrokerBaselineValidator {
    return BrokerBaselineValidator(
        mutableListOf<BrokerBaselineRule>(
            BrokerBaselineCodeLengthRule(32),
            BrokerBaselineRequiredLabelRule("broker baseline needs an entry"),
            BrokerBaselineAllowedTrailerIdRule(mutableListOf<String>("primary", "scheduled")),
        ),
    )
}
