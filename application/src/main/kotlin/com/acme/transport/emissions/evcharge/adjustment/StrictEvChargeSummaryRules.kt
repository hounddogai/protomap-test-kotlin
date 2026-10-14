package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a strict ev charge summary must pass. Check returns its problems, or none. */
interface StrictEvChargeSummaryRule {
    fun check(item: StrictEvChargeSummary): MutableList<String>
}

data class StrictEvChargeSummaryCodePrefixRule(var prefix: String) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSummaryRequiredCodeRule(var message: String) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSummaryRequiredSegmentRule(var message: String) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSummaryRequiredLabelRule(var message: String) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSummaryCodeLengthRule(var maxLength: Int) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeSummaryAllowedLabelRule(var allowed: MutableList<String>) : StrictEvChargeSummaryRule {
    override fun check(item: StrictEvChargeSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge summary. */
data class StrictEvChargeSummaryValidator(var rules: MutableList<StrictEvChargeSummaryRule>) {
    fun validate(item: StrictEvChargeSummary): MutableList<String> {
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

    fun requireValid(item: StrictEvChargeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeSummaryValidator(): StrictEvChargeSummaryValidator {
    return StrictEvChargeSummaryValidator(
        mutableListOf<StrictEvChargeSummaryRule>(
            StrictEvChargeSummaryCodePrefixRule("Q"),
            StrictEvChargeSummaryRequiredCodeRule("strict ev charge summary needs an entry"),
            StrictEvChargeSummaryRequiredSegmentRule("strict ev charge summary needs a value"),
            StrictEvChargeSummaryRequiredLabelRule("strict ev charge summary needs this field"),
            StrictEvChargeSummaryCodeLengthRule(32),
            StrictEvChargeSummaryAllowedLabelRule(mutableListOf<String>("pending", "weighted", "global")),
        ),
    )
}
