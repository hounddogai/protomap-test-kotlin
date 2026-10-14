package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.normalize

/** A rule a transit time threshold must pass. Check returns its problems, or none. */
interface TransitTimeThresholdRule {
    fun check(item: TransitTimeThreshold): MutableList<String>
}

data class TransitTimeThresholdCodeLengthRule(var maxLength: Int) : TransitTimeThresholdRule {
    override fun check(item: TransitTimeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeThresholdRequiredPhoneRule(var message: String) : TransitTimeThresholdRule {
    override fun check(item: TransitTimeThreshold): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeThresholdAllowedNotesRule(var allowed: MutableList<String>) : TransitTimeThresholdRule {
    override fun check(item: TransitTimeThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time threshold. */
data class TransitTimeThresholdValidator(var rules: MutableList<TransitTimeThresholdRule>) {
    fun validate(item: TransitTimeThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeThresholdValidator(): TransitTimeThresholdValidator {
    return TransitTimeThresholdValidator(
        mutableListOf<TransitTimeThresholdRule>(
            TransitTimeThresholdCodeLengthRule(86400),
            TransitTimeThresholdRequiredPhoneRule("transit time threshold needs an entry"),
            TransitTimeThresholdAllowedNotesRule(mutableListOf<String>()),
        ),
    )
}
