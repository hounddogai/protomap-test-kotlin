package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize

/** A rule a redelivery segment must pass. Check returns its problems, or none. */
interface RedeliverySegmentRule {
    fun check(item: RedeliverySegment): MutableList<String>
}

data class RedeliverySegmentCodePrefixRule(var prefix: String) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySegmentDriverIdLengthRule(var maxLength: Int) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySegmentRequiredTitleRule(var message: String) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySegmentAllowedCodeRule(var allowed: MutableList<String>) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySegmentRequiredLabelRule(var message: String) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySegmentLabelLengthRule(var maxLength: Int) : RedeliverySegmentRule {
    override fun check(item: RedeliverySegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery segment. */
data class RedeliverySegmentValidator(var rules: MutableList<RedeliverySegmentRule>) {
    fun validate(item: RedeliverySegment): MutableList<String> {
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

    fun requireValid(item: RedeliverySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliverySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliverySegmentValidator(): RedeliverySegmentValidator {
    return RedeliverySegmentValidator(
        mutableListOf<RedeliverySegmentRule>(
            RedeliverySegmentCodePrefixRule("REF"),
            RedeliverySegmentDriverIdLengthRule(16),
            RedeliverySegmentRequiredTitleRule("redelivery segment needs an entry"),
            RedeliverySegmentAllowedCodeRule(mutableListOf<String>()),
            RedeliverySegmentRequiredLabelRule("redelivery segment needs an entry"),
            RedeliverySegmentLabelLengthRule(16),
        ),
    )
}
