package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard threshold must pass. Check returns its problems, or none. */
interface TrailerYardThresholdRule {
    fun check(item: TrailerYardThreshold): MutableList<String>
}

data class TrailerYardThresholdAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardThresholdRule {
    override fun check(item: TrailerYardThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardThresholdRequiredCarrierRule(var message: String) : TrailerYardThresholdRule {
    override fun check(item: TrailerYardThreshold): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardThresholdRequiredTitleRule(var message: String) : TrailerYardThresholdRule {
    override fun check(item: TrailerYardThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardThresholdCodeLengthRule(var maxLength: Int) : TrailerYardThresholdRule {
    override fun check(item: TrailerYardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard threshold. */
data class TrailerYardThresholdValidator(var rules: MutableList<TrailerYardThresholdRule>) {
    fun validate(item: TrailerYardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardThresholdValidator(): TrailerYardThresholdValidator {
    return TrailerYardThresholdValidator(
        mutableListOf<TrailerYardThresholdRule>(
            TrailerYardThresholdAllowedCodeRule(mutableListOf<String>("seasonal")),
            TrailerYardThresholdRequiredCarrierRule("trailer yard threshold needs this field"),
            TrailerYardThresholdRequiredTitleRule("trailer yard threshold needs a value"),
            TrailerYardThresholdCodeLengthRule(120),
        ),
    )
}
