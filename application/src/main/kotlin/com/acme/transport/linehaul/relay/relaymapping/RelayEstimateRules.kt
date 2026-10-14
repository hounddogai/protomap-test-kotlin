package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize

/** A rule a relay estimate must pass. Check returns its problems, or none. */
interface RelayEstimateRule {
    fun check(item: RelayEstimate): MutableList<String>
}

data class RelayEstimateRequiredDockRule(var message: String) : RelayEstimateRule {
    override fun check(item: RelayEstimate): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayEstimateAllowedCodeRule(var allowed: MutableList<String>) : RelayEstimateRule {
    override fun check(item: RelayEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RelayEstimateCodeLengthRule(var maxLength: Int) : RelayEstimateRule {
    override fun check(item: RelayEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay estimate. */
data class RelayEstimateValidator(var rules: MutableList<RelayEstimateRule>) {
    fun validate(item: RelayEstimate): MutableList<String> {
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

    fun requireValid(item: RelayEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayEstimateValidator(): RelayEstimateValidator {
    return RelayEstimateValidator(
        mutableListOf<RelayEstimateRule>(
            RelayEstimateRequiredDockRule("relay estimate needs this field"),
            RelayEstimateAllowedCodeRule(mutableListOf<String>("capped", "fallback", "primary")),
            RelayEstimateCodeLengthRule(80),
        ),
    )
}
