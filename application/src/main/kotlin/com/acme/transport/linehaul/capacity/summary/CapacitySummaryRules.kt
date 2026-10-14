package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize

/** A rule a capacity summary must pass. Check returns its problems, or none. */
interface CapacitySummaryRule {
    fun check(item: CapacitySummary): MutableList<String>
}

data class CapacitySummarySegmentLengthRule(var maxLength: Int) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySummaryAllowedSegmentRule(var allowed: MutableList<String>) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySummaryCodePrefixRule(var prefix: String) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySummaryCodeLengthRule(var maxLength: Int) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySummaryRequiredReviewerRule(var message: String) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacitySummaryReviewerLengthRule(var maxLength: Int) : CapacitySummaryRule {
    override fun check(item: CapacitySummary): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity summary. */
data class CapacitySummaryValidator(var rules: MutableList<CapacitySummaryRule>) {
    fun validate(item: CapacitySummary): MutableList<String> {
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

    fun requireValid(item: CapacitySummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacitySummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacitySummaryValidator(): CapacitySummaryValidator {
    return CapacitySummaryValidator(
        mutableListOf<CapacitySummaryRule>(
            CapacitySummarySegmentLengthRule(3600),
            CapacitySummaryAllowedSegmentRule(mutableListOf<String>("adaptive")),
            CapacitySummaryCodePrefixRule("REF"),
            CapacitySummaryCodeLengthRule(255),
            CapacitySummaryRequiredReviewerRule("capacity summary needs an entry"),
            CapacitySummaryReviewerLengthRule(120),
        ),
    )
}
